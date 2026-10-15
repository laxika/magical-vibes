package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ECL", collectorNumber = "73")
public class SummitSentinel extends Card {

    public SummitSentinel() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
