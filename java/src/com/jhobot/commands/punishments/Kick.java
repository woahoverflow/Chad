package com.jhobot.commands.punishments;

import com.jhobot.JhoBot;
import com.jhobot.handle.DB;
import com.jhobot.handle.Messages;
import com.jhobot.handle.Util;
import com.jhobot.handle.commands.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Kick implements Command {
    @Override
    public Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            Messages m = new Messages(e.getChannel());
            if (!e.getClient().getOurUser().getPermissionsForGuild(e.getGuild).contains(Permissions.KICK))
            {
                m.sendError("The bot doesn't have permissions for this!");
                return;
            }
            if (!e.getAuthor().getPermissionsForGuild(e.getGuild).contains(Permissions.KICK))
            {
                m.sendError("You don't have permissions for this!");
                return;
            }
            IUser user = null;
            List<String> reason = new ArrayList<>();
            if (!e.getMessage().getMentions().isEmpty() && args.get(0).equalsIgnoreCase(e.getMessage().getMentions().get(1).mention()))
            {
                user = e.getMessage().getMentions().get(0);
                args.remove(0);
                reason = args;
            }
            else {
                StringBuilder sb = new StringBuilder();
                for (String s : args)
                {
                    if (user == null)
                    {
                        sb.append(s + " ");
                        if (!e.getGuild().getUsersByName(sb.toString().trim()).isEmpty())
                        {
                            user = e.getGuild().getUsersByName(sb.toString().trim()).get(0);
                        }
                    }
                    else {
                        reason.add(s);
                    }
                }
            }

            if (user == null)
            {
                m.sendError("Invalid User!");
                return;
            }

            if (user.getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR))
            {
                m.sendError("Bot can't do this!");
                return;
            }
            
            
            if (PermissionUtils.hasHierarchicalPermissions(e.getChannel(), e.getClient().getOurUser(), e.getAuthor(), Permissions.KICK)
            {
                m.sendError("Bot can't do this!");
                return;
            }

            StringBuilder sb2 = new StringBuilder();
            for (String s : reason)
            {
                sb2.append(s + " ");
            }

            String ban_message = JhoBot.db.getString(e.getGuild(), "kick_message");
            if (!ban_message.toLowerCase().contains("&disabled&") && !user.isBot())
            {
                String[] msgsArray = JhoBot.db.getString(e.getGuild(), "kick_message").split(" ");
                List<String> msgs = Arrays.asList(msgsArray);
                for (int i = 0; msgs.size() > i; i++)
                {
                    if (msgs.get(i).toLowerCase().contains("&guild&"))
                        msgs.set(i, e.getGuild().getName() + msgs.get(i).substring(7));
                    if (msgs.get(i).toLowerCase().contains("&user&"))
                        msgs.set(i, user.getName() + msgs.get(i).substring(6));
                    if (msgs.get(i).toLowerCase().contains("&reason&"))
                        msgs.set(i, sb2.toString().trim() + msgs.get(i).substring(8));
                    if (msgs.get(i).toLowerCase().contains("&newline&"))
                        msgs.set(i, "\n" + msgs.get(i).substring(8));
                }
                StringBuilder sb = new StringBuilder();
                for (String s : msgs)
                {
                    sb.append(s + " ");
                }

                new MessageBuilder(e.getClient()).withChannel(e.getClient().getOrCreatePMChannel(user)).withContent(sb.toString().trim()).build();
            }

            if (reason.isEmpty())
            {
                reason.add("None");
                m.send("Successfully kicked " + user.getName() + " for no reason.", "Kicked User");
                m.sendPunishLog("Kick", user, e.getAuthor(), JhoBot.db, e.getGuild(), reason);
                return;
            }
            // 
                
                
                
               
            e.getGuild().kickUser(user);
            m.send("Successfully kicked " + user.getName() + " for " + sb2.toString().trim() + ".", "Kicked User");
            m.sendPunishLog("Kick", user, e.getAuthor(), JhoBot.db, e.getGuild(), reason);
        };
    }

    @Override
    public Runnable help(MessageReceivedEvent e, List<String> args) {
        return () -> {
            EmbedBuilder b = new EmbedBuilder();
            b.withTitle("Help : Kick");
            b.appendField(JhoBot.db.getString(e.getGuild(), "prefix") + "kick <user/@user> [reason]", "Kicks a user with a reason.", false);
            b.withFooterText(Util.getTimeStamp());
            b.withColor(new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
            new Messages(e.getChannel()).sendEmbed(b.build());
        };
    }
}