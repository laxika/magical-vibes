package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Triggered ability / emblem marker: whenever you cast a spell matching {@code spellFilter},
 * deal damage equal to the amount of mana spent to cast that spell to any target.
 * <p>
 * Place in {@code ON_CONTROLLER_CASTS_SPELL} on a permanent, or store in an {@link Emblem}'s
 * static effects (Chandra, Dressed to Kill −7). The collector / emblem loop snapshots mana spent
 * at cast time into a fixed {@link DealDamageToAnyTargetEffect} and queues any-target selection.
 *
 * @param spellFilter which spells trigger this (e.g. {@code CardColorPredicate(RED)}; null = any)
 */
public record DealDamageEqualToManaSpentToCastToAnyTargetEffect(
        CardPredicate spellFilter
) implements CardEffect {
}
