package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;

@CardRegistration(set = "LRW", collectorNumber = "64")
@CardRegistration(set = "MM3", collectorNumber = "37")
public class FamiliarsRuse extends Card {

    public FamiliarsRuse() {
        // As an additional cost to cast this spell, return a creature you control to its owner's hand.
        addCastingCost(new ReturnCreatureToHandCost());
        // Counter target spell.
        addSpellEffect(new CounterSpellEffect());
    }
}
