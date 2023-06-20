package io.github.eatmyvenom.litematicin.mixin.Litematica;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import fi.dy.masa.litematica.util.WorldUtils;
import io.github.eatmyvenom.litematicin.utils.Printer;
import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.Minecraft;
//import net.minecraft.util.ActionResultType;
import net.minecraft.util.ActionResult;

@Mixin(value = WorldUtils.class, remap = false)
public class WorldUtilsMixin {
    
    @Overwrite
    private static ActionResult doEasyPlaceAction(MinecraftClient mc)
    {
        return Printer.doPrinterAction(mc);
    }
}
