package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HBG", collectorNumber = "H24")
public class HagOfSyphonedBreath extends Card {

    public HagOfSyphonedBreath() {
        // At the beginning of your upkeep, each opponent loses 2 life and you gain 2 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2)));
    }
}
