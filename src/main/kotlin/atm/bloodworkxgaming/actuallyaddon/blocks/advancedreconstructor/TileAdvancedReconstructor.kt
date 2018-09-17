package atm.bloodworkxgaming.actuallyaddon.blocks.advancedreconstructor

import atm.bloodworkxgaming.bloodyLib.energy.EnergyStorageBase
import atm.bloodworkxgaming.bloodyLib.tile.TileEntityBase
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI
import de.ellpeck.actuallyadditions.api.recipe.LensConversionRecipe
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagInt
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.ItemStackHandler
import java.util.*
import kotlin.math.min

class TileAdvancedReconstructor : TileEntityBase(), ITickable {
    companion object {
        val recipes: List<LensConversionRecipe> = ActuallyAdditionsAPI.RECONSTRUCTOR_LENS_CONVERSION_RECIPES
        const val energyModifier = 1.5
    }

    private val stackHandlerInput = ItemStackHandlerInput()
    private val stackHandlerOutput = ItemStackHandlerOutput()
    private val energyStorage = EnergyStorageBase(10000, 10, 0)


    init {
        recipes.forEach { println("it = ${Arrays.toString(it.input.matchingStacks)} -> ${it.output} # ${it.type}") }
    }

    private var counter = 0
    override fun update() {
        world ?: return

        if (counter++ <= 10 || world.isRemote)
            return

        counter = 0


        println("energy ${energyStorage.energyStored}")

        val stack = stackHandlerInput.getStackInSlot(0)
        println("stack in input $stack")

        if (stack.isEmpty) {
            return
        }

        val inCount = stack.count
        val recipe = recipes.firstOrNull { it.matches(stack, ActuallyAdditionsAPI.lensDefaultConversion) }

        println("recipe = $recipe")

        recipe ?: return

        // checks how much energy there is to craft stuff
        val energyCount = (energyStorage.energyStored / (recipe.energyUsed * energyModifier)).toInt()
        if (energyCount <= 0) return

        // checks how many fit in the output slot
        val outCount = recipe.output.count
        val outStack = recipe.output.copy()
        val extractCount = outCount * min(inCount, energyCount)
        outStack.count = extractCount

        // checks how much it can insert and then actually does the insertion
        val leftOver = stackHandlerOutput.insertItemInternal(0, outStack)
        val effectiveCount = extractCount - leftOver.count
        stackHandlerInput.extractItemInternal(0, effectiveCount)
        val energyCost = (effectiveCount * recipe.energyUsed * energyModifier).toInt()
        println("effectiveCount = $energyCost")
        energyStorage.extractEnergyInternal(energyCost, false)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        return capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability === CapabilityEnergy.ENERGY
                || super.hasCapability(capability, facing)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {

        return when {
            capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> when (facing) {
                NORTH, SOUTH, WEST, EAST -> stackHandlerInput as T
                UP, DOWN -> stackHandlerOutput as T
                else -> null
            }

            capability === CapabilityEnergy.ENERGY -> return energyStorage as T

            else -> super.getCapability(capability, facing)
        }
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("input", stackHandlerInput.serializeNBT())
        compound.setTag("output", stackHandlerOutput.serializeNBT())
        compound.setTag("energy", energyStorage.serializeNBT())

        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        if (compound.hasKey("input"))
            stackHandlerInput.deserializeNBT(compound.getCompoundTag("input"))
        if (compound.hasKey("output"))
            stackHandlerInput.deserializeNBT(compound.getCompoundTag("output"))

        if (compound.hasKey("energy"))
            energyStorage.deserializeNBT(compound.getTag("energy") as NBTTagInt?)

        super.readFromNBT(compound)
    }

    inner class ItemStackHandlerOutput : ItemStackHandler(1) {
        override fun onContentsChanged(slot: Int) = markDirty()
        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean) = stack
        internal fun insertItemInternal(slot: Int, stack: ItemStack) = super.insertItem(slot, stack, false)
    }

    inner class ItemStackHandlerInput : ItemStackHandler(1) {
        override fun onContentsChanged(slot: Int) = markDirty()
        override fun extractItem(slot: Int, amount: Int, simulate: Boolean) = ItemStack.EMPTY!!
        internal fun extractItemInternal(slot: Int, amount: Int) = super.extractItem(slot, amount, false)
    }
}
