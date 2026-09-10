package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Capability for a static effect that grants flash to the first matching spell each turn. */
public interface FirstMatchingSpellFlashGrant extends CardEffect {

    CardPredicate predicate();
}
