package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfTwoTargetCreaturesThenPutCountersOnOtherEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STH", collectorNumber = "53")
@CardRegistration(set = "TPR", collectorNumber = "83")
public class Cannibalize extends Card {

    public Cannibalize() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileOneOfTwoTargetCreaturesThenPutCountersOnOtherEffect());
        target(TargetFilters.creature());
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
