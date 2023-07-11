package me.jensvh.litematica.printer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

/**
 * This class contains the printer implementation.
 */
public class Printer
{

    /**
     * This method implements the printer.
     *
     * @param mc the MinecraftClient instance
     * @return the ActionResult
     */
    public static ActionResult doEasyPlaceAction(MinecraftClient mc)
    {
        System.out.println("This line is printed by the printer!");
        return ActionResult.PASS;

    }

}
