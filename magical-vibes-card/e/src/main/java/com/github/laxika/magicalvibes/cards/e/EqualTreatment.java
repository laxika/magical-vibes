package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdThisTurnEffect;

@CardRegistration(set = "TOR", collectorNumber = "4")
public class EqualTreatment extends Card {

    public EqualTreatment() {
        addSpellEffect(new ReplaceDamageAboveThresholdThisTurnEffect(1, 2));
        addSpellEffect(new DrawCardEffect());
    }
}
