package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "FDN", collectorNumber = "68")
public class SanguineSyphoner extends Card {

    public SanguineSyphoner() {
        // Whenever this creature attacks, each opponent loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_ATTACK, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(1));
    }
}
