package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "BOK", collectorNumber = "87")
public class ThreeTragedies extends Card {

    public ThreeTragedies() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER));
    }
}
