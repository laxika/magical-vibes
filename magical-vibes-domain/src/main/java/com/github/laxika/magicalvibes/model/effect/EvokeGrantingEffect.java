package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** A static effect that grants a fixed evoke cost to matching permanent spells. */
public interface EvokeGrantingEffect extends CardEffect {

    String evokeCost();

    CardPredicate evokeGrantFilter();
}
