/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.commands.*;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import util.Const;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import static platform.discord.controller.DiscordController.sendToChannel;

/**
 * @author keesh
 */
public class CommandParser {
    public static HashMap<String, Command> commands = new HashMap<>();

    CommandParser() {

        // Register core.commands with the bot
        commands.put("add", new Add());
        commands.put("announce", new Announce());
        commands.put("compact", new Compact());
        commands.put("help", new Help());
        commands.put("invite", new Invite());
        commands.put("move", new Move());
        commands.put("notify", new Notify());
        commands.put("ping", new Ping());
        commands.put("remove", new Remove());
        commands.put("streams", new Streams());
    }

    /**
     * @return the core.commands
     */
    public static HashMap<String, Command> getCommands() {

        return commands;
    }

    /**
     * @param aCommands the core.commands to set
     */
    public static void setCommands(HashMap<String, Command> aCommands) {

        commands = aCommands;
    }

    /**
     * @param cmd Object containing required arguments to invoke the command
     */
    public static void handleCommand(CommandParser.CommandContainer cmd) throws PropertyVetoException, IOException, SQLException {

        if (getCommands().containsKey(cmd.invoke)) {

            boolean safe = getCommands().get(cmd.invoke).called(cmd.args, cmd.event);

            if (safe) {

                // TODO: Match the capitalisation of ping and return in pong

                if (cmd.args != null && cmd.args.equals("help")) {
                    getCommands().get(cmd.invoke).help(cmd.event);
                } else {
                    getCommands().get(cmd.invoke).action(cmd.args, cmd.event);
                }
            } else {
                sendToChannel(cmd.event, Const.INCORRECT_ARGS);
            }
            getCommands().get(cmd.invoke).executed(safe, cmd.event);
        }
    }

    public CommandContainer parse(String raw, MessageReceivedEvent event) throws PropertyVetoException, SQLException, IOException {
        String beheaded = raw.replaceFirst(Const.COMMAND_PREFIX, "");  // Remove COMMAND_PREFIX

        String removeCommand;
        String invoke = null;
        String args = null;

        if (beheaded.contains(" ")) {
            removeCommand = beheaded.substring(beheaded.indexOf(" ") + 1); // Remove Const.COMMAND {add opt opt}

            if (removeCommand.contains(" ")) {
                invoke = removeCommand.substring(0, removeCommand.indexOf(" ")); // Return just the command
                args = removeCommand.substring(removeCommand.indexOf(" ") + 1);
            } else {
                // Send to commands with no args
                invoke = removeCommand;
            }
        } else if ("ping".equals(beheaded)) {
            invoke = beheaded;
            args = null;
        } else {
            sendToChannel(event, Const.EMPTY_COMMAND);
        }

        return new CommandContainer(invoke, args, event);
    }

    private static class CommandContainer {

        public final String args;
        public final MessageReceivedEvent event;
        private final String invoke;

        CommandContainer(String invoke, String args, MessageReceivedEvent event) {
            this.invoke = invoke.toLowerCase(); // The Command (ensure the command is always passes as lowercase)
            this.args = args; // Command Arguments
            this.event = event; // The Event
        }
    }
}
