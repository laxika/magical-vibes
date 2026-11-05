package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchAllCreaturesPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "24")
public class MangledSoulrager extends Card {

    public MangledSoulrager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SwitchAllCreaturesPowerToughnessEffect(true),
                new CreateBoonEffect(12, new SwitchPowerToughnessEffect(false, true)));
        addCycling("{1}{U}");
    }
}
