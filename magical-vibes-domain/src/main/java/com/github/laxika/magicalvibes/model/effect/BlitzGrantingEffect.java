package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** A static effect that grants a fixed blitz cost to matching creature spells cast from hand. */
public interface BlitzGrantingEffect extends CardEffect {

    String blitzCost();

    CardPredicate blitzGrantFilter();
}
