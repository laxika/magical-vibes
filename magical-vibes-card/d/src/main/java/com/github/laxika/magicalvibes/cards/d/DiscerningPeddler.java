package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "OTJ", collectorNumber = "121")
public class DiscerningPeddler extends Card {

    public DiscerningPeddler() {
        // When this creature enters, you may discard a card. If you do, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
