package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect placed on a Curse Aura that enchants a player. The enchanted player can't
 * cast more than {@code maxSpells} spells each turn. Unlike {@link LimitSpellsPerTurnEffect}
 * (Rule of Law), which limits every player globally, this only limits the enchanted player.
 *
 * <p>Used by Curse of Exhaustion. When multiple limits apply to a player, the most
 * restrictive (lowest) value wins.
 */
public record LimitSpellsForEnchantedPlayerEffect(int maxSpells) implements CardEffect {
}
