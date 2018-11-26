package org.woahoverflow.chad.commands.fun;

import java.security.SecureRandom;
import org.json.JSONArray;
import org.woahoverflow.chad.core.ChadVar;
import org.woahoverflow.chad.handle.MessageHandler;
import org.woahoverflow.chad.handle.commands.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;

public class EightBall implements Command.Class  {
    @Override
    public final Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            MessageHandler messageHandler = new MessageHandler(e.getChannel());

            // Makes sure they asked a question
            if (args.isEmpty())
            {
                messageHandler.sendError("You didn't ask a question!");
                return;
            }

            // Gets the answers from the cdn
            JSONArray answers = ChadVar.JSON_DEVICE.readArray("https://cdn.woahoverflow.org/chad/data/8ball.json");

            // Sends the answer
            messageHandler.send((String) answers.get(new SecureRandom().nextInt(answers.length())), "8Ball");
        };
    }

    @Override
    public final Runnable help(MessageReceivedEvent e) {
        HashMap<String, String> st = new HashMap<>();
        st.put("8ball <question>", "The eight ball always answers your best questions.");
        return Command.helpCommand(st, "Eight Ball", e);
    }
}
