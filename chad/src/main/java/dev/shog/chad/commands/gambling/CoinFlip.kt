package dev.shog.chad.commands.gambling

import kotlinx.coroutines.delay
import dev.shog.chad.framework.handle.GuildHandler
import dev.shog.chad.framework.handle.MessageHandler
import dev.shog.chad.framework.handle.PlayerHandler
import dev.shog.chad.framework.handle.coroutine.asIMessage
import dev.shog.chad.framework.handle.coroutine.asIReaction
import dev.shog.chad.framework.handle.coroutine.request
import dev.shog.chad.framework.obj.Command
import dev.shog.chad.framework.obj.Guild
import dev.shog.chad.framework.obj.Player.DataType
import dev.shog.chad.framework.util.validBet
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder
import java.util.*
import kotlin.random.Random

/**
 * Flips a coin with Chad, or with another user
 *
 * @author sho
 */
class CoinFlip : Command.Class {
    override suspend fun run(e: MessageEvent, args: MutableList<String>) {
        val messageHandler = MessageHandler(e.channel, e.author)
        val author = PlayerHandler.getPlayer(e.author.longID)
        val prefix = GuildHandler.getGuild(e.guild.longID).getObject(Guild.DataType.PREFIX) as String

        if (e.message.mentions.isEmpty() && args.size == 2) {
            // Makes sure the bot is a valid long
            val bet: Long = try {
                args[0].toLong()
            } catch (ex: NumberFormatException) {
                messageHandler.sendError("Invalid Bet!")
                return
            }

            val balance = author.getObject(DataType.BALANCE) as Long
            if (!validBet(bet, balance)) {
                messageHandler.sendError("Invalid Bet!")
                return
            }

            val user: Int = when {
                args[1].equals("heads", ignoreCase = true) -> 0
                args[1].equals("tails", ignoreCase = true) -> 1

                else -> {
                    messageHandler.sendError("Please use `heads` or `tails`!")
                    return
                }
            }

            val flip = Random.nextInt(2)

            if (flip == user) {
                author.setObject(DataType.BALANCE, balance + bet)
                messageHandler.sendEmbed(EmbedBuilder().withDesc("You won `$bet`, you now have `${balance+bet}`!"))
            } else {
                author.setObject(DataType.BALANCE, balance - bet)
                messageHandler.sendEmbed(EmbedBuilder().withDesc("You lost `$bet`, you now have `${balance-bet}`!"))
            }
            return
        } else if (e.message.mentions.size == 1 && args.size == 2) {
            val user: IUser = if (e.message.mentions.isNotEmpty() && args[1].contains(e.message.mentions[0].stringID)) {
                e.message.mentions[0]
            } else {
                messageHandler.sendPresetError(MessageHandler.Messages.INVALID_ARGUMENTS, "${prefix}coinflip [@user] [bet]")
                return
            }

            if (user == e.author || user == e.client.ourUser) {
                messageHandler.sendPresetError(MessageHandler.Messages.INVALID_ARGUMENTS, "${prefix}coinflip [@user] [bet]")
                return
            }

            val bet: Long = try {
                args[0].toLong()
            } catch (ex: NumberFormatException) {
                messageHandler.sendError("Invalid Bet!")
                return
            }

            val acceptMessage = request {
                e.channel.sendMessage("Do you accept `${e.author.name}`'s challenge, `${user.name}`?")
            }.asIMessage()

            val rb = RequestBuilder(e.client)
            rb.shouldBufferRequests(true)
            rb.doAction {
                acceptMessage.addReaction(ReactionEmoji.of("\uD83C\uDDFE")) // Y
                true
            }.andThen {
                acceptMessage.addReaction(ReactionEmoji.of("\uD83C\uDDF3")) // N
                true
            }.execute() // Executes

            var reacted = false
            var timeout = 0

            while (!reacted) {
                if (timeout == 10) {
                    messageHandler.sendError("`${user.name}` didn't respond in time!")
                    return
                }

                delay(1000L)

                timeout++

                val yReaction = request {
                    acceptMessage.getReactionByEmoji(ReactionEmoji.of("\uD83C\uDDFE"))
                }.asIReaction()

                val nReaction = request {
                    acceptMessage.getReactionByEmoji(ReactionEmoji.of("\uD83C\uDDF3"))
                }.asIReaction()

                if (yReaction.getUserReacted(user)) reacted = true

                if (nReaction.getUserReacted(user)) {
                    messageHandler.sendError("`${user.name}` denied!")
                    return
                }
            }

            val otherUser = PlayerHandler.getPlayer(user.longID)

            val otherBalance = otherUser.getObject(DataType.BALANCE) as Long
            val balance = author.getObject(DataType.BALANCE) as Long
            if (!validBet(bet, balance, otherBalance = otherBalance)) {
                messageHandler.sendError("Invalid Bet!")
                return
            }

            val pick = request {
                e.channel.sendMessage("**X** TAILS\n**O** HEADS")
            }.asIMessage()

            // Request buffer to apply the reactions
            val r = RequestBuilder(e.client)
            r.shouldBufferRequests(true)

            r.doAction {
                pick.addReaction(ReactionEmoji.of("\uD83C\uDDFD")) // X
                true
            }.andThen {
                pick.addReaction(ReactionEmoji.of("\uD83C\uDDF4")) // O
                true
            }.execute()

            timeout = 0
            var tails: IUser? = null
            var heads: IUser? = null
            while (tails == null || heads == null) {
                delay(10000L)

                if (timeout * 100 * 10 > 10) {
                    messageHandler.sendError("`${user.name}` or `${e.author.name}` didn't respond in time!")
                    return
                }

                // X reaction
                val x = request {
                    pick.getReactionByEmoji(ReactionEmoji.of("\uD83C\uDDFD"))
                }.asIReaction()

                // O reaction
                val o = request {
                    pick.getReactionByEmoji(ReactionEmoji.of("\uD83C\uDDF4"))
                }.asIReaction()

                if (tails == null) {
                    if (x.getUserReacted(user)) {
                        if (heads != user) tails = user
                    }

                    if (x.getUserReacted(e.author)) {
                        if (heads != e.author) tails = e.author
                    }
                }

                if (heads == null) {
                    if (o.getUserReacted(user)) {
                        if (tails != user) heads = user
                    }

                    if (o.getUserReacted(e.author)) {
                        if (tails != e.author) heads = e.author
                    }
                }
            }

            RequestBuffer.request {
                pick.delete()
            }

            val tailsBalance: Long = if (e.author == tails) balance else otherBalance
            val headsBalance: Long = if (e.author == heads) balance else otherBalance

            val flip = Random.nextInt(2)

            // 0 = Tails; 1 = Heads
            if (flip == 0) {
                PlayerHandler.getPlayer(tails.longID).setObject(DataType.BALANCE, tailsBalance + bet)
                PlayerHandler.getPlayer(heads.longID).setObject(DataType.BALANCE, headsBalance - bet)

                messageHandler.sendMessage("`${tails.name}` has won `$bet`!\n\n" +
                        "`${tails.name}` now has `${tailsBalance + bet}`, and `${heads.name}` now has `${headsBalance - bet}`.")
            } else {
                PlayerHandler.getPlayer(tails.longID).setObject(DataType.BALANCE, tailsBalance - bet)
                PlayerHandler.getPlayer(heads.longID).setObject(DataType.BALANCE, headsBalance + bet)

                messageHandler.sendMessage("`${heads.name}` has won `$bet`!\n\n" +
                        "`${heads.name}` now has `${headsBalance + bet}`, and `${tails.name}` now has `${tailsBalance - bet}`.")

            }
        } else {
            messageHandler.sendPresetError(MessageHandler.Messages.INVALID_ARGUMENTS, "${prefix}coinflip [amount to bet] [@user] / ${prefix}coinflip [amount to bet] [tails/head]")
        }
    }

    override suspend fun help(e: MessageEvent) {
        val st = HashMap<String, String>()
        st["coinflip [amount to bet] [tails/heads]"] = "Play CoinFlip with Chad"
        st["coinflip [amount to bet] [@user]"] = "Play CoinFlip with another user"
        Command.helpCommand(st, "CoinFlip", e)
    }
}
