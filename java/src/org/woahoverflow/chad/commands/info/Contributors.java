package org.woahoverflow.chad.commands.info;

import org.woahoverflow.chad.core.ChadVar;
import org.woahoverflow.chad.handle.MessageHandler;
import org.woahoverflow.chad.handle.Util;
import org.woahoverflow.chad.handle.commands.Command;
import org.woahoverflow.chad.handle.commands.HelpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Contributors implements Command {
    @SuppressWarnings("all")
    @Override
    public Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            EmbedBuilder b = new EmbedBuilder();
            JSONArray o = ChadVar.JSON_HANDLER.readArray("https://raw.githubusercontent.com/woahoverflow/Chad-Repo/master/data/contributors.json");
            if (args.size() == 2 && args.get(0).equalsIgnoreCase("view"))
            {
                JSONObject object = null;
                for (Object obj : o)
                {
                    if (((JSONObject) obj).getString("display_name").equalsIgnoreCase(args.get(2)))
                    {
                        object = (JSONObject) obj;
                    }
                }

                if (object == null)
                {
                    new MessageHandler(e.getChannel()).sendError("Invalid User");
                }

                // builds profile
                b.withUrl("https://github.com/" + object.getString("github_name"));
                b.withTitle("https://github.com/" + object.getString("display_name"));
                b.withImage(object.getString("github_avatar_url"));
                b.withDesc("Discord : " + object.getString("discord"));
                new MessageHandler(e.getChannel()).sendEmbed(b.build());
                return;
            }
            StringBuilder b2 = new StringBuilder();
            b.withTitle("Official Contributors for Chad");
            o.forEach((obj) -> {
                JSONObject json = (JSONObject) obj;
                b2.append(json.getString("display_name")).append(", ");
            });
            b.withDesc(b2.toString().trim());
            b.withColor(new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
            b.withFooterText(Util.getTimeStamp());
            new MessageHandler(e.getChannel()).sendEmbed(b.build());
        };
    }

    @Override
    public Runnable help(MessageReceivedEvent e, List<String> args) {
        HashMap<String, String> st = new HashMap<>();
        st.put("contributors", "Gets all contributors that've committed to Chad.");
        return HelpHandler.helpCommand(st, "Contributors", e);
    }
}
