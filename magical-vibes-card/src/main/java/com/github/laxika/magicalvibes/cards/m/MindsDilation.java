package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect;

@CardRegistration(set = "EMN", collectorNumber = "70")
public class MindsDilation extends Card {

    public MindsDilation() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect());
    }
}
