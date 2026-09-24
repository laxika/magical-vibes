package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Generic controller spell-cast trigger restricted to spells cast from hand.
 */
public record SpellCastFromHandTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
