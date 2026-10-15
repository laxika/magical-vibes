package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "199")
@CardRegistration(set = "WOT", collectorNumber = "58")
@CardRegistration(set = "WOT", collectorNumber = "83")
@CardRegistration(set = "WOT", collectorNumber = "103")
@CardRegistration(set = "MAR", collectorNumber = "36")
@CardRegistration(set = "OMB", collectorNumber = "36")
public class ParallelLives extends Card {

    public ParallelLives() {
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
    }
}
