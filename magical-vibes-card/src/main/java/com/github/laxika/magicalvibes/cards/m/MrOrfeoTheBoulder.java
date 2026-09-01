package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "204")
public class MrOrfeoTheBoulder extends Card {

    public MrOrfeoTheBoulder() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new DoubleTargetCreaturePowerEffect(new Fixed(1)));
    }
}
