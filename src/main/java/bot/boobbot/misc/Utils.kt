package bot.boobbot.misc

import bot.boobbot.BoobBot
import bot.boobbot.BoobBot.Companion.getMusicManager
import bot.boobbot.flight.Command
import de.mxro.metrics.jre.Metrics
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.managers.GuildManager
import okhttp3.Headers
import org.json.JSONObject
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.text.MessageFormat
import java.time.Instant.now
import java.util.*
import javax.imageio.ImageIO


class Utils {
    companion object {
        private val rand = Random()

        private val ips = Arrays.asList(
                "5.231.237.168:3213",
                "94.249.224.97:2543",
                "185.164.57.91:4012",
                "185.164.57.144:9749",
                "185.164.57.70:5756"
        )

        private val jsonArrays = JSONObject(
                (BoobBot::class.java.classLoader.getResourceAsStream("arrays.json"))
                        .bufferedReader()
                        .use { it.readText() }
        )

        fun isDonor(user: User): Boolean {
            val member = BoobBot.home?.getMember(user) ?: return false
            return member.roles.any { r -> r.idLong == 440542799658483713L }
        }

        fun getRandomFunString(key: String): String {
            val arr = jsonArrays.getJSONArray(key)
            return arr.getString(rand.nextInt(arr.length()))
        }

        fun getRandomMoan(): File {
            val arr = jsonArrays.getJSONArray("moan")
            val fileOjb = arr.getJSONObject(rand.nextInt(arr.length()))
            return (File("/root/moan/${fileOjb.get("name")}.${fileOjb.get("ext")}"))
        }

        fun getRandomAvatar(): InputStream {
            val arr = jsonArrays.getJSONArray("avatar")
            val fileOjb = arr.getJSONObject(rand.nextInt(arr.length()))
            return (BoobBot::class.java.classLoader.getResourceAsStream("avatar/${fileOjb.get("name")}.${fileOjb.get("ext")}"))
        }

        fun getProxy(): Proxy {
            val proxy = ips[rand.nextInt(ips.size)]
            val parts = proxy.split(":".toRegex(), 2).toTypedArray()
            return Proxy(Proxy.Type.HTTP, InetSocketAddress(parts[0], parts[1].toInt()))
        }

        fun disconnectFromVoice(channel: VoiceChannel) {
            getMusicManager(channel.guild).shutdown()
        }

        fun connectToVoiceChannel(message: Message) {
            if (!message.guild.audioManager.isConnected && !message.guild.audioManager.isAttemptingToConnect) {
                message.guild.audioManager.openAudioConnection(message.member.voiceState.channel)
            }
        }


        fun logCommand(message: net.dv8tion.jda.core.entities.Message) =
                if ((message.isFromType(ChannelType.PRIVATE))) {
                    val msg = MessageFormat.format(
                            "{4}: {0} Used {1} on Channel: {2}({3})",
                            message.author.name,
                            message.contentRaw,
                            message.channel.name,
                            message.channel.id,
                            now())
                    BoobBot.log.info(msg)
                } else {
                    val msg = MessageFormat.format(
                            "{6}: {0} Used {1} on Guild:{4}({5}) in Channel: {2}({3})",
                            message.author.name,
                            message.contentRaw,
                            message.channel.name,
                            message.channel.id,
                            message.guild.name,
                            message.guild.id,
                            now())
                    BoobBot.log.info(msg)
                }


        fun getCommand(commandName: String): Command? {
            val commands = BoobBot.commands
            return commands[commandName]
                    ?: commands.values.firstOrNull { it.properties.aliases.contains(commandName) }
        }

        fun downloadAvatar(url: String): BufferedImage? {
            val body = BoobBot.requestUtil.get(url, Headers.of()).block()?.body()
                    ?: return null

            var image: BufferedImage? = null

            body.byteStream().use {
                image = ImageIO.read(it)
            }

            return image
        }

        fun fTime(milliseconds: Long): String {
            val seconds = milliseconds / 1000 % 60
            val minutes = milliseconds / (1000 * 60) % 60
            val hours = milliseconds / (1000 * 60 * 60) % 24
            val days = milliseconds / (1000 * 60 * 60 * 24)

            return when {
                days > 0 -> String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
                hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
                else -> String.format("%02d:%02d", minutes, seconds)
            }
        }

        fun autoAvatar() {
            val icon = Icon.from(getRandomAvatar())
            val gm = GuildManager(BoobBot.home)
            gm.setIcon(icon).queue()
            BoobBot.shardManager.shards[0].selfUser.manager.setAvatar(icon).queue()
            BoobBot.log.info("Setting New Guild icon/Avatar")
        }

        fun auto(autoAvatar: Unit): Runnable = Runnable { autoAvatar() }
    }
}


