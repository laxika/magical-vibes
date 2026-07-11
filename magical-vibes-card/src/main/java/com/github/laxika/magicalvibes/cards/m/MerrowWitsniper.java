package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "MOR", collectorNumber = "40")
public class MerrowWitsniper extends Card {

    public MerrowWitsniper() {
        // When this creature enters, target player mills a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(1, MillRecipient.TARGET_PLAYER));
    }
}
