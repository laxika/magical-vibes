package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;

@CardRegistration(set = "EXO", collectorNumber = "63")
public class Grollub extends Card {

    public Grollub() {
        // Whenever this creature is dealt damage, each opponent gains that much life.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new GainLifeEffect(new EventValue(), GainLifeRecipient.OPPONENT));
    }
}
