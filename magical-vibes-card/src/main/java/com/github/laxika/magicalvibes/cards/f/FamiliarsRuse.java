package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;

@CardRegistration(set = "LRW", collectorNumber = "64")
public class FamiliarsRuse extends Card {

    public FamiliarsRuse() {
        // As an additional cost to cast this spell, return a creature you control to its owner's hand.
        addEffect(EffectSlot.SPELL, new ReturnCreatureToHandCost());
        // Counter target spell.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
