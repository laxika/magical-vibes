package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "9ED", collectorNumber = "146")
public class Mindslicer extends Card {

    public Mindslicer() {
        // When this creature dies, each player discards their hand.
        addEffect(EffectSlot.ON_DEATH, new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
    }
}
