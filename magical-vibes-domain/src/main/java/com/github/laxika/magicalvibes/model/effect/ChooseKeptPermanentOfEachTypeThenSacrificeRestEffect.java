package com.github.laxika.magicalvibes.model.effect;

/**
 * Tragic Arrogance: "For each player, you choose from among the permanents that player controls an
 * artifact, a creature, an enchantment, and a planeswalker. Then each player sacrifices all other
 * nonland permanents they control."
 *
 * <p>The spell's controller makes every choice, one player and one card type at a time (artifact →
 * creature → enchantment → planeswalker). A type the player controls nothing of is skipped, and a
 * permanent with several of those types may be chosen for more than one of them (Gatherer
 * 2015-06-22), which is why earlier picks are not removed from later candidate lists. All choices
 * are made before anything is sacrificed, and the sacrifices then happen simultaneously. Driven by
 * {@code ChooseKeptPermanentOfEachTypeThenSacrificeRestEffectHandler}.
 */
public record ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect() implements CardEffect {
}
