package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** A static effect that grants a fixed prowl cost to matching spells. */
public interface ProwlGrantingEffect extends CardEffect {

    String prowlCost();

    CardPredicate prowlGrantFilter();
}
