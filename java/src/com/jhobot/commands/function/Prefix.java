package com.jhobot.commands.function;

import com.jhobot.JhoBot;
import com.jhobot.handle.Messages;
import com.jhobot.handle.DB;
import com.jhobot.handle.Util;
import com.jhobot.handle.commands.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Prefix implements Command {
    @Override
    public Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            Messages m = new Messages(e.getChannel());
            if (!e.getAuthor().getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR))
            {
                m.sendError("You don't have permission for this!");
                return;
            }
            if (args.size() == 0) {
                EmbedBuilder b = new EmbedBuilder();
                b.withTitle("Prefix");
                b.withDesc("Your prefix is " + JhoBot.db.getString(e.getGuild(), "prefix"));
                b.withFooterText(Util.getTimeStamp());
                b.withColor(new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
                m.sendEmbed(b.build());
                return;
            }

            if (args.size() == 2 && args.get(0).equalsIgnoreCase("set"))
            {
                if (args.get(1).length() > 12)
                {
                    new Messages(e.getChannel()).sendError("Prefix can't be over 12 characters long!");
                }
                m.sendConfigLog("Prefix", args.get(1), JhoBot.db.getString(e.getGuild(), "prefix"), e.getAuthor(), e.getGuild(), JhoBot.db);
                JhoBot.db.set(e.getGuild(), "prefix", args.get(1));
                m.send("Your prefix is now " + args.get(1), "Changed Prefix");
                return;
            }

            m.sendError("Invalid Arguments");
        };
    }

    @Override
    public Runnable help(MessageReceivedEvent e, List<String> args) {
        return () -> {
            EmbedBuilder b = new EmbedBuilder();
            b.withTitle("Help : Prefix");
            b.appendField(JhoBot.db.getString(e.getGuild(), "prefix")+"prefix", "Gives information about your prefixes.", false);
            b.appendField(JhoBot.db.getString(e.getGuild(), "prefix") + "prefix set <prefix>", "Sets your guild's prefix.", false);
            b.withFooterText(Util.getTimeStamp());
            b.withColor(new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
            new Messages(e.getChannel()).sendEmbed(b.build());
        };
    }
}
