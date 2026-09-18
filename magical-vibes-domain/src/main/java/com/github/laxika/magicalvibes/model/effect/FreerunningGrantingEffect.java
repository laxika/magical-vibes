package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a fixed freerunning cost to matching spells. */
public interface FreerunningGrantingEffect extends CardEffect {

    String freerunningCost();

    CardPredicate freerunningGrantFilter();
}
