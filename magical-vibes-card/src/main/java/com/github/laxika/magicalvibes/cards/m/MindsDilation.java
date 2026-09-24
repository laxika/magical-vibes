package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect;

@CardRegistration(set = "EMN", collectorNumber = "70")
@CardRegistration(set = "SIR", collectorNumber = "82")
@CardRegistration(set = "MSC", collectorNumber = "277")
@CardRegistration(set = "MSC", collectorNumber = "344")
public class MindsDilation extends Card {

    public MindsDilation() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect());
    }
}
