package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Emblem marker for "Whenever a creature attacks you, it gets +power/+toughness and gains
 * {@code keywords} until end of turn." (Garruk, Apex Predator's emblem).
 *
 * <p>Lives in an {@code Emblem}'s static effects, never on a card. {@code CombatAttackService}
 * scans the emblems of the player being attacked <em>directly</em> (the emblem does not cover
 * attacks on that player's planeswalkers) and pushes a non-targeting triggered ability carrying a
 * {@link BoostTargetCreatureEffect} plus a {@link GrantKeywordEffect} aimed at the attacking
 * creature, once per attacking creature.
 */
public record BoostAttackingCreatureOnAttacksYouEffect(int powerBoost, int toughnessBoost,
                                                       Set<Keyword> keywords) implements CardEffect {
}
