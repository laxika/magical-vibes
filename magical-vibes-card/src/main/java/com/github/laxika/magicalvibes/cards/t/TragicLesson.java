package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessReturnLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOU", collectorNumber = "51")
public class TragicLesson extends Card {

    public TragicLesson() {
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));

        // Then discard a card unless you return a land you control to its owner's hand.
        addEffect(EffectSlot.SPELL, new DiscardUnlessReturnLandToHandEffect());
    }
}
