package com.willfp.ecobits.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.Currency
import com.willfp.ecobits.currencies.getBalance
import com.willfp.ecobits.currencies.setBalance
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class CommandReset(
    plugin: EcoPlugin,
    private val currency: Currency? = null
) : Subcommand(
    plugin,
    "reset",
    "ecobits.command.reset",
    false
) {
    private val argOffset = if (currency == null) 0 else -1

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("must-specify-player"))
            return
        }

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(args[0])

        if (!player.hasPlayedBefore() && !player.isOnline) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        if (this.currency == null) {
            if (args.size < 2) {
                sender.sendMessage(plugin.langYml.getMessage("must-specify-currency"))
                return
            }
        }

        val currency = this.currency ?: Currencies.getByID(args[1].lowercase())

        if (currency == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        player.setBalance(currency, currency.default)

        sender.sendMessage(
            plugin.langYml.getMessage("reset-currency", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%amount%", currency.default.toNiceString())
                .replace("%currency%", currency.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                completions
            )
        }

        if (this.currency == null) {
            if (args.size == 2) {
                StringUtil.copyPartialMatches(
                    args[1],
                    Currencies.values().map { it.id },
                    completions
                )
            }
        }

        return completions
    }
}
