package dev.shog.chad.commands.developer

import kotlinx.coroutines.delay
import dev.shog.chad.core.getClient
import dev.shog.chad.core.getLogger
import dev.shog.chad.framework.handle.MessageHandler
import dev.shog.chad.framework.handle.coroutine.asIMessage
import dev.shog.chad.framework.handle.coroutine.request
import dev.shog.chad.framework.obj.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder
import java.util.*

/**
 * Shuts down the bot via discord
 *
 * @author sho
 */
class Shutdown : Command.Class {
    override suspend fun run(e: MessageEvent, args: MutableList<String>) {
        val messageHandler = MessageHandler(e.channel, e.author)

        // Requests to send the confirmation message then gets it
        val confirm = request { e.channel.sendMessage("Are you sure you want to do this?") }.asIMessage()

        // The emojis used in the message
        val yes = ReactionEmoji.of("\uD83C\uDDFE")
        val no = ReactionEmoji.of("\uD83C\uDDF3")

        // Adds both reactions
        val builder = RequestBuilder(e.client)
        builder.shouldBufferRequests(true)
        builder.doAction {
            confirm.addReaction(yes)
            true
        }.andThen {
            confirm.addReaction(no)
            true
        }.execute()

        var userReacted = false
        var timeout = 0
        while (!userReacted) {
            delay(1000L)

            // If the user's taken more than 10 seconds
            if (timeout >= 10) {
                messageHandler.sendError("You didn't react fast enough!")
                return
            }

            // Add another second
            timeout++

            // If they've accepted
            if (confirm.getReactionByEmoji(yes).getUserReacted(e.author))
                userReacted = true

            // If they've denied
            if (confirm.getReactionByEmoji(no).getUserReacted(e.author)) {
                messageHandler.sendError("Cancelled shutdown!")
                return
            }
        }

        // Deletes the confirmation message
        RequestBuffer.request { confirm.delete() }

        // Warns that the bot is shutting down
        MessageHandler(e.channel, e.author).sendEmbed(EmbedBuilder().withDesc("Chad is shutting down in 10 seconds..."))

        // Warns within the UI
        getLogger().warn("Shutting down in 10 seconds...")

        // Updates the presence
        getClient().changePresence(StatusType.DND, ActivityType.PLAYING, "Shutting down...")

        delay(10000L)

        // Exits
        getClient().logout()
        System.exit(0)
    }

    override suspend fun help(e: MessageEvent) {
        val hash = HashMap<String, String>()
        hash["shutdown"] = "Shuts the bot down."
        Command.helpCommand(hash, "Shutdown", e)
    }
}
