package org.woahoverflow.chad.commands.info;

import org.woahoverflow.chad.handle.MessageHandler;
import org.woahoverflow.chad.handle.commands.Command;
import org.woahoverflow.chad.handle.commands.HelpHandler;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Chad implements Command
{
    @Override
    public Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            EmbedBuilder b = new EmbedBuilder();
            b.withTitle("Chad");
            b.withDesc("by sho and CodeBase!");
            b.appendField("GitHub", "https://github.com/shoganeko/Chad", true);
            b.withColor(new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
            new MessageHandler(e.getChannel()).sendEmbed(b.build());
        };
    }

    @Override
    public Runnable help(MessageReceivedEvent e, List<String> args) {
        HashMap<String, String> st = new HashMap<>();
        st.put("org/woahoverflow/chad", "Gives information about the bot.");
        return HelpHandler.helpCommand(st, "Chad", e);
    }
}