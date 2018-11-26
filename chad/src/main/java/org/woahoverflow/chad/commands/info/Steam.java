package org.woahoverflow.chad.commands.info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woahoverflow.chad.core.ChadVar;
import org.woahoverflow.chad.handle.MessageHandler;
import org.woahoverflow.chad.handle.commands.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;
import java.util.List;

public class Steam implements Command.Class  {
    @Override
    public final Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            MessageHandler messageHandler = new MessageHandler(e.getChannel());
            
            // Gets the steam api token from the bot.json
            String key = ChadVar.JSON_DEVICE.get("steam_api_token");

            // Checks if the arguments are invalid
            if (args.isEmpty() || args.size() == 1)
            {
                messageHandler.sendError(MessageHandler.INVALID_ARGUMENTS);
                return;
            }

            try {
                // builds steam profile
                
                // The intention of the command
                String intention = args.get(0);
                
                // Gets the user's steam id by their username
                JSONObject steamUser = ChadVar.JSON_DEVICE.read("https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + key + "&vanityurl=" + args.get(1))
                    .getJSONObject("response");
                
                // Checks to see if the user actually exists
                if (steamUser.getInt("success") != 1)
                {
                    messageHandler.sendError("Invalid Steam Profile!");
                    return;
                }

                // Gets the user's steam id
                String steamId = steamUser.getString("steamid");
                
                // Gets the user's profile
                JSONObject steamUserProfile = ChadVar.JSON_DEVICE.read("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + key + "&steamids=" + steamId).getJSONObject("response").getJSONArray("players").getJSONObject(0);
                
                // The user's name
                String userName = steamUserProfile.getString("personaname");
                

                // Builds an embed with the user's steam profile
                if (intention.equalsIgnoreCase("profile"))
                {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.withTitle("Steam Profile : " + userName);
                    embedBuilder.withImage(steamUserProfile.getString("avatarfull"));
                    embedBuilder.appendField("SteamID", steamId, true);

                    messageHandler.sendEmbed(embedBuilder);
                } 
                else if (intention.equalsIgnoreCase("csgo")) // Builds an embed with the user's Counter-Strike: Global Offensive stats.
                {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.withFooterIcon(steamUserProfile.getString("avatar"));
                    
                    JSONArray csgoStats = ChadVar.JSON_DEVICE.read(
                        "https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=730&key="
                            + key + "&steamid=" + steamId).getJSONObject("playerstats")
                        .getJSONArray("stats");
                    
                    // Makes sure the array isn't null
                    if (csgoStats == null)
                    {
                        messageHandler.sendError(MessageHandler.INTERNAL_EXCEPTION);
                        return;
                    }
                    // Checks to see if the account is private by catching the NullPointerException
                    try {
                        csgoStats.getJSONObject(0);
                    } catch (@SuppressWarnings("ProhibitedExceptionCaught") NullPointerException ee)
                    {
                        messageHandler.sendError("Private Steam Profile!");
                        return;
                    }
                    
                    
                    if (args.size() == 2) // Regular CS:GO stats
                    {
                        embedBuilder.withTitle("CS:GO stats for " + userName);
                        embedBuilder.appendField("Total Kills", Integer.toString(csgoStats.getJSONObject(0).getInt("value")), true);
                        embedBuilder.appendField("Total Deaths", Integer.toString(csgoStats.getJSONObject(1).getInt("value")), true);
                        embedBuilder.appendField("Total Time Played", csgoStats.getJSONObject(2).getInt("value") / 60 / 60 + "hrs", true);
                        embedBuilder.appendField("Total Bombs Planted", Integer.toString(csgoStats.getJSONObject(3).getInt("value")), true);
                        embedBuilder.appendField("Total Bombs Defused", Integer.toString(csgoStats.getJSONObject(4).getInt("value")), true);
                        embedBuilder.appendField("Total Wins", Integer.toString(csgoStats.getJSONObject(5).getInt("value")), true);
                        embedBuilder.appendField("Total Damage Done", Integer.toString(csgoStats.getJSONObject(6).getInt("value")), true);
                        embedBuilder.appendField("Total Matches Won", Integer.toString(csgoStats.getJSONObject(112).getInt("value")), true);
                        embedBuilder.appendField("Total Matches Played", Integer.toString(csgoStats.getJSONObject(113).getInt("value")), true);
                    }
                    else if (args.size() == 3 && args.get(2).equalsIgnoreCase("kills")) // User's kill stats in CS:GO
                    {
                        embedBuilder.withTitle("Map Stats for " + userName);
                        embedBuilder.appendField("Knife", Integer.toString(csgoStats.getJSONObject(9).getInt("value")), true);
                        embedBuilder.appendField("Taser", Integer.toString(csgoStats.getJSONObject(166).getInt("value")), true);
                        embedBuilder.appendField("HE Grenade", Integer.toString(csgoStats.getJSONObject(10).getInt("value")), true);
                        embedBuilder.appendField("Glock", Integer.toString(csgoStats.getJSONObject(11).getInt("value")), true);
                        embedBuilder.appendField("Deagle", Integer.toString(csgoStats.getJSONObject(12).getInt("value")), true);
                        embedBuilder.appendField("USP-S", Integer.toString(csgoStats.getJSONObject(133).getInt("value")), true);
                        embedBuilder.appendField("Mac-10", Integer.toString(csgoStats.getJSONObject(16).getInt("value")), true);
                        embedBuilder.appendField("UMP-45", Integer.toString(csgoStats.getJSONObject(17).getInt("value")), true);
                        embedBuilder.appendField("AWP", Integer.toString(csgoStats.getJSONObject(19).getInt("value")), true);
                        embedBuilder.appendField("AK-47", Integer.toString(csgoStats.getJSONObject(20).getInt("value")), true);
                        embedBuilder.appendField("M4", Integer.toString(csgoStats.getJSONObject(162).getInt("value")), true);
                    } else if (args.size() == 3 && args.get(2).equalsIgnoreCase("maps")) // User's map stats in CS:GO
                    {
                        embedBuilder.withTitle("Map Wins for " + userName);
                        embedBuilder.appendField("Office", Integer.toString(csgoStats.getJSONObject(28).getInt("value")), true);
                        embedBuilder.appendField("Cobble", Integer.toString(csgoStats.getJSONObject(29).getInt("value")), true);
                        embedBuilder.appendField("Dust 2", Integer.toString(csgoStats.getJSONObject(30).getInt("value")), true);
                        embedBuilder.appendField("Inferno", Integer.toString(csgoStats.getJSONObject(31).getInt("value")), true);
                        embedBuilder.appendField("Train", Integer.toString(csgoStats.getJSONObject(33).getInt("value")), true);
                        embedBuilder.appendField("Nuke", Integer.toString(csgoStats.getJSONObject(32).getInt("value")), true);
                    } else if (args.size() == 3 && args.get(2).equalsIgnoreCase("lastmatch")) // User's last match in CS:GO
                    {
                        embedBuilder.withTitle(userName + "'s last game");
                        embedBuilder.appendField("Round Wins", Integer.toString(csgoStats.getJSONObject(81).getInt("value") + csgoStats.getJSONObject(82).getInt("value")), true);
                        embedBuilder.appendField("Kills", Integer.toString(csgoStats.getJSONObject(85).getInt("value")), true);
                        embedBuilder.appendField("Deaths", Integer.toString(csgoStats.getJSONObject(86).getInt("value")), true);
                        embedBuilder.appendField("MVPs", Integer.toString(csgoStats.getJSONObject(87).getInt("value")), true);

                    }
                    else {
                        // If none of those were selected
                        messageHandler.sendError(MessageHandler.INVALID_ARGUMENTS);
                        return;
                    }

                    // Sends message
                    messageHandler.sendEmbed(embedBuilder);
                } else {
                    // If neither the profile or CS:GO were accessed
                    messageHandler.sendError(MessageHandler.INVALID_ARGUMENTS);
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (RuntimeException ee)
            {
                if (ee.getMessage().contains("429"))
                {
                    messageHandler.sendError("Too Many Requests!");
                    return;
                }
                messageHandler.sendError("There was an throwError with that request!");
                ee.printStackTrace();
            }
        };
    }

    @Override
    public final Runnable help(MessageReceivedEvent e) {
        HashMap<String, String> st = new HashMap<>();
        st.put("steam profile <steam name>", "Gets a steam user's profile.");
        st.put("steam csgo <steam name> [kills/maps/lastmatch]", "Gets a steam user's CS:GO stats.");
        return Command.helpCommand(st, "Steam", e);
    }
}
