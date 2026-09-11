package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "DSK", collectorNumber = "11")
public class FearOfSurveillance extends Card {

    public FearOfSurveillance() {
        addEffect(EffectSlot.ON_ATTACK, new SurveilEffect(1));
    }
}
