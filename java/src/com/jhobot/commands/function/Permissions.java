package com.jhobot.commands.function;

import com.jhobot.core.ChadBot;
import com.jhobot.handle.LogLevel;
import com.jhobot.handle.MessageHandler;
import com.jhobot.handle.commands.*;
import com.jhobot.handle.commands.permissions.PermissionHandler;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Permissions implements Command {
    @Override
    public Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            MessageHandler m = new MessageHandler(e.getChannel());
            // j!perms role <role> add <perm>
            if (args.size() >= 3 && args.get(0).equalsIgnoreCase("role"))
            {
                args.remove(0); // removes so it can get role name
                StringBuilder b = new StringBuilder();
                List<IRole> r = new ArrayList<>();
                int i = 0;
                int i1 = 0;
                for (String s : args)
                {
                    i++;
                    b.append(s).append(" ");

                    r = RequestBuffer.request(() -> e.getGuild().getRolesByName(b.toString().trim())).get();
                    if (!r.isEmpty()) break;
                }

                if (args.size() == i)
                {
                    m.sendError("Invalid Role");
                    return;
                }
                IRole role = r.get(0);

                // isolates next arguments
                while (i > i1)
                {
                    i1++;
                    args.remove(0);
                }

                String nextArg = args.get(0);
                System.out.println(nextArg);
                args.remove(0); // isolates again
                if (nextArg.equalsIgnoreCase("add"))
                {
                    try {
                        PermissionHandler.HANDLER.addCommandToRole(role, args.get(0));
                        m.send("Added `" + args.get(0) + "` command to role `" + role.getName() + "`.", "Permissions");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else if (nextArg.equalsIgnoreCase("remove") && args.size() >= 4)
                {
                    int rem = PermissionHandler.HANDLER.removeCommandFromRole(role, args.get(0));
                    if (rem == 6)
                    {
                        m.send("Permissions", "Removed `" + args.get(0) + "` command to role `" + role.getName() + "`.");
                    }
                    else {
                        m.sendError(PermissionHandler.HANDLER.parseErrorCode(rem));
                    }
                }
                else if (nextArg.equalsIgnoreCase("view"))
                {
                    if (ChadBot.DATABASE_HANDLER.getArray(e.getGuild(), role.getStringID()) == null)
                    {
                        m.sendError("There's no permissions there!");
                        return;
                    }
                    EmbedBuilder b2 = new EmbedBuilder();
                    b2.withTitle("Viewing Permissions for `" + role.getName()+"`");
                    StringBuilder b3 = new StringBuilder();
                    ChadBot.DATABASE_HANDLER.getArray(e.getGuild(), role.getStringID()).forEach((v) -> b3.append(v).append(", "));
                    b2.withDesc(b3.toString().substring(b3.toString().length(), b3.toString().length()-2));
                    m.sendEmbed(b2.build());
                }
            }
        };
    }

    @Override
    public Runnable help(MessageReceivedEvent e, List<String> args) {
        return null;
    }

    @Override
    public PermissionLevels level() {
        return PermissionLevels.SYSTEM_ADMINISTRATOR;
    }

    @Override
    public Category category() {
        return Category.FUNCTION;
    }
}
