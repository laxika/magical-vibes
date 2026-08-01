package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "RTR", collectorNumber = "112")
public class ViashinoRacketeer extends Card {

    public ViashinoRacketeer() {
        // When Viashino Racketeer enters, you may discard a card. If you do, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));
    }
}
