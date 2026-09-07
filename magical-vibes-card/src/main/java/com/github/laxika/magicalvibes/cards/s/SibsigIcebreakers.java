package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "DTK", collectorNumber = "119")
public class SibsigIcebreakers extends Card {

    public SibsigIcebreakers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
