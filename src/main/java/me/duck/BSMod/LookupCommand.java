package me.duck.BSMod;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class LookupCommand extends CommandBase implements ICommand{
    @Override
    public String getCommandName() {
        return "lookup";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }


    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "Lookup good flips :D";
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        BSMod e = new BSMod();

        if (BSMod.apiKey.equals("None")){
            IChatComponent text = new ChatComponentText("Please set your API key by using /setapi <key>");
            Minecraft.getMinecraft().thePlayer.addChatMessage(text);
            return;
        }
        e.onPlayerTick();

    }



}
