package me.jensvh.litematica.printer;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import me.jensvh.litematica.printer.mixins.ConfigOptionsMixin;
import net.fabricmc.api.ModInitializer;

public class PrinterMod implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("Hello Fabric world!");
    }
    
}
