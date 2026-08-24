package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Triggered ability: whenever a matching spell is cast,
 * this permanent deals damage equal to that spell's mana value to any target.
 * <p>
 * Used in a spell-cast trigger slot. The trigger handler computes the damage
 * from the cast spell's mana value and queues target selection.
 *
 * @param spellFilter which spells trigger this (e.g. PhyrexianManaPredicate)
 */
public record DealDamageEqualToSpellManaValueToAnyTargetEffect(
        CardPredicate spellFilter
) implements CardEffect {
}
