package atm.bloodworkxgaming.actuallyaddon.blocks.advancedfluidizer

import atm.bloodworkxgaming.actuallyaddon.ActuallyAddon
import atm.bloodworkxgaming.actuallyaddon.gui.GuiBase
import net.minecraft.client.gui.Gui
import net.minecraft.inventory.Container
import net.minecraft.util.ResourceLocation

class GUIAdvancedFluidizer(tileEntity: TileAdvancedFluidizer, container: Container)
    : GuiBase<TileAdvancedFluidizer>(tileEntity, container, BACKGROUND) {


    init {
        xSize = WIDTH
        ySize = HEIGHT
    }

    companion object {
        const val WIDTH = 180
        const val HEIGHT = 178
        const val GUI_ID = 2
        const val deviceName = "Advanced Reconstructor"

        val BACKGROUND = ResourceLocation(ActuallyAddon.MOD_ID, "textures/gui/advanced_reconstructor_gui.png")
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int, offsetX: Int, offsetY: Int) {
        // Render RF Bar
        val stored = tile.energyStorage.energyStored
        val storedInput = tile.fluidTankInput.fluidAmount
        val storedOutput = tile.fluidTankOutput.fluidAmount
        val maxStored = tile.fluidTankInput.capacity

        if (stored > 0) {
            Gui.drawRect(offsetX + 12,
                    (offsetY + 23 + 38 * (1 - stored.toDouble() / tile.energyStorage.maxEnergyStored)).toInt(),
                    offsetX + 24,
                    offsetY + 61, -0x1000000 or 0x700b0b)
        }

        if (stored > 0) {
            Gui.drawRect(offsetX + 12 + 50,
                    (offsetY + 23 + 38 * (1 - storedInput.toDouble() / maxStored)).toInt(),
                    offsetX + 24 + 50,
                    offsetY + 61, -0x1000000 or 0x700b0b)
        }

        if (stored > 0) {
            Gui.drawRect(offsetX + 12 + 100,
                    (offsetY + 23 + 38 * (1 - storedOutput.toDouble() / maxStored)).toInt(),
                    offsetX + 24 + 100,
                    offsetY + 61, -0x1000000 or 0x700b0b)
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int, offsetX: Int, offsetY: Int) {
        // Render title
        fontRenderer.drawString(deviceName, xSize / 2 - fontRenderer.getStringWidth(deviceName) / 2, 6, 4210752)

        renderHoveredToolTip(mouseX - offsetX, mouseY - offsetY)

        // Render Hovering RF text
        if (mouseX >= 12 + offsetX && mouseX <= 24 + offsetX && mouseY >= 23 + offsetY && mouseY <= 61 + offsetY) {
            val text = "${tile.energyStorage.energyStored}/${tile.energyStorage.maxEnergyStored} FE"

            drawHoveringText(text, mouseX - offsetX, mouseY - offsetY)
        }
    }
}