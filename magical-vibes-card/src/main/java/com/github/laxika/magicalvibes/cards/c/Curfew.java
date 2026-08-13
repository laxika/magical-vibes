package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCreatureToHandEffect;

@CardRegistration(set = "USG", collectorNumber = "68")
public class Curfew extends Card {

    public Curfew() {
        // Each player returns a creature they control to its owner's hand.
        addEffect(EffectSlot.SPELL, new EachPlayerReturnsCreatureToHandEffect());
    }
}
