package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect placed on a Curse Aura that enchants a player.
 * If a source would deal damage to the enchanted player, it deals double that damage
 * to that player instead. Unlike {@link DoubleDamageEffect} (Furnace of Rath), which
 * doubles all damage globally, this only doubles damage dealt to the enchanted player.
 *
 * <p>Used by Curse of Bloodletting. Multiple instances stack multiplicatively.
 */
public record DoubleDamageToEnchantedPlayerEffect() implements CardEffect {
}
