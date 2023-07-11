package me.jensvh.litematica.printer.mixins;

import me.jensvh.litematica.printer.Printer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

/**
 * This class is used to inject the printing code into Litematica.
 * 
 */
@Mixin(value = WorldUtils.class, remap = false)
public class LitematicaMixin {

    /**
     * This method overwrites the litematica printer.
     */
    @Overwrite
    private static ActionResult doEasyPlaceAction(MinecraftClient mc)
    {
        return Printer.doEasyPlaceAction(mc);
    }
    
}
