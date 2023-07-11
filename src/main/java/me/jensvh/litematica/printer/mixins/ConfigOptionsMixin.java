package me.jensvh.litematica.printer.mixins;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;

/**
 * This class is used to change the litematica config options.
 */
@Mixin (value = Configs.Generic.class, remap = false)
public abstract class ConfigOptionsMixin
{

    @Shadow
    @Final
    public static ImmutableList<IConfigBase> OPTIONS;

    static {
        ArrayList<IConfigBase> options = new ArrayList<>(OPTIONS);

        // Remove the build in options
        options.remove(Configs.Generic.EASY_PLACE_PROTOCOL);
        options.remove(Configs.Generic.EASY_PLACE_FIRST);
        options.remove(Configs.Generic.EASY_PLACE_SP_HANDLING);
        options.remove(Configs.Generic.EASY_PLACE_VANILLA_REACH);
        options.remove(Configs.Generic.EASY_PLACE_SWAP_INTERVAL);

        // Add our own options


        OPTIONS = ImmutableList.copyOf(options);
    }

}
