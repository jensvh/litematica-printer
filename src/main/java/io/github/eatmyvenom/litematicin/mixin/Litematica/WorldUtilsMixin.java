package io.github.eatmyvenom.litematicin.mixin.Litematica;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import fi.dy.masa.litematica.util.WorldUtils;
import io.github.eatmyvenom.litematicin.utils.Printer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ActionResultType;

@Mixin(value = WorldUtils.class, remap = false)
public class WorldUtilsMixin {
    /**
     * @author joe mama
     */
    @Overwrite
    private static ActionResultType doEasyPlaceAction(Minecraft mc)
    {
        return Printer.doPrinterAction(mc);
    }
}
