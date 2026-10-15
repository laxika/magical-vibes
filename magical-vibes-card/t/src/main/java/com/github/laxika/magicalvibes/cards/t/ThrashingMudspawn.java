package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ONS", collectorNumber = "177")
public class ThrashingMudspawn extends Card {

    public ThrashingMudspawn() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER));
    }
}
