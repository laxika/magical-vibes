package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "204")
public class MrOrfeoTheBoulder extends Card {

    public MrOrfeoTheBoulder() {
        target(TargetFilters.creature()).addTrigger(Triggers.youAttack(),
                new DoubleTargetCreaturePowerEffect(new Fixed(1)));
    }
}
