package dev.shog.chad.commands.punishments

import dev.shog.chad.framework.handle.GuildHandler
import dev.shog.chad.framework.handle.MessageHandler
import dev.shog.chad.framework.obj.Command
import dev.shog.chad.framework.obj.Guild.DataType
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.PermissionUtils
import java.util.*
import java.util.regex.Pattern

/**
 * Moderator tool to ban a user
 *
 * @author sho
 */
class Ban : Command.Class {
    override suspend fun run(e: MessageEvent, args: MutableList<String>) {
        val messageHandler = MessageHandler(e.channel, e.author)

        val guild = GuildHandler.getGuild(e.guild.longID)

        // Checks if the bot has permission to ban
        if (!e.client.ourUser.getPermissionsForGuild(e.guild).contains(Permissions.BAN)) {
            messageHandler.sendPresetError(MessageHandler.Messages.BOT_NO_PERMISSION)
            return
        }

        // Forms user from author's mentions
        val user: IUser
        val reason: MutableList<String>
        if (e.message.mentions.isNotEmpty() && args[0].contains(e.message.mentions[0].stringID)) {
            user = e.message.mentions[0]
            args.removeAt(0)
            reason = args
        } else {
            messageHandler.sendPresetError(MessageHandler.Messages.INVALID_USER)
            return
        }

        // Checks if the user action upon has administrator
        if (user.getPermissionsForGuild(e.guild).contains(Permissions.ADMINISTRATOR)) {
            messageHandler.sendPresetError(MessageHandler.Messages.BOT_NO_PERMISSION)
            return
        }

        // Checks if bot has hierarchical permissions
        if (!PermissionUtils.hasHierarchicalPermissions(e.channel, e.client.ourUser, user, Permissions.BAN)) {
            messageHandler.sendPresetError(MessageHandler.Messages.BOT_NO_PERMISSION)
            return
        }

        // Checks if user has hierarchical permissions
        if (!PermissionUtils.hasHierarchicalPermissions(e.channel, e.client.ourUser, user, Permissions.BAN)) {
            messageHandler.sendPresetError(MessageHandler.Messages.USER_NO_PERMISSION)
            return
        }

        // Builds reason
        val builtReason = StringBuilder()
        if (reason.isNotEmpty())
            for (s in reason)
                builtReason.append("$s ")
        else
            builtReason.append("no reason")

        // Checks if ban message is enabled
        if (guild.getObject(DataType.BAN_MESSAGE_ON) as Boolean) {
            // Gets the message from the cache
            val message = guild.getObject(DataType.BAN_MESSAGE) as String

            // If the message isn't null, continue
            var formattedMessage = GUILD_PATTERN.matcher(message).replaceAll(e.guild.name) // replaces &guild& with guild's name
            formattedMessage = USER_PATTERN.matcher(formattedMessage).replaceAll(user.name) // replaces &user& with user's name
            formattedMessage = REASON_PATTERN.matcher(formattedMessage).replaceAll(builtReason.toString().trim { it <= ' ' }) // replaces &reason& with the reason

            // If the user isn't bot, send the message.
            if (!user.isBot)
                MessageBuilder(e.client).withChannel(e.client.getOrCreatePMChannel(user)).withContent(formattedMessage).build()
        }

        // If there's no reason, continue with "no reason"
        if (reason.isEmpty()) {
            e.guild.banUser(user)
            reason.add("None")
            messageHandler.sendEmbed(EmbedBuilder().appendDesc("Successfully banned " + user.name + " for no reason."))
            MessageHandler.sendPunishLog("Ban", user, e.author, e.guild, reason)
            return
        }

        // Bans the user.
        e.guild.banUser(user)
        messageHandler.sendEmbed(EmbedBuilder().withDesc("Successfully banned " + user.name + " for " + builtReason.toString().trim { it <= ' ' } + '.'.toString()))
        MessageHandler.sendPunishLog("Ban", user, e.author, e.guild, reason)

    }

    override suspend fun help(e: MessageEvent) {
        val st = HashMap<String, String>()
        st["ban [@user]"] = "Bans a user with no reason."
        st["ban [@user] [reason]"] = "Bans a user with a specified reason."
        Command.helpCommand(st, "Ban", e)
    }

    companion object {

        // Patterns for the message forming
        private val GUILD_PATTERN = Pattern.compile("&guild&")
        private val USER_PATTERN = Pattern.compile("&user&")
        private val REASON_PATTERN = Pattern.compile("&reason&")
    }
}
