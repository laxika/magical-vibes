package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "137")
public class Fugue extends Card {

    public Fugue() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER));
    }
}
