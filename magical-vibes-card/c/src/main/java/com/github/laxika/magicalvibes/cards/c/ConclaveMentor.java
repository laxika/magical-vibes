package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingSourcePowerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "216")
@CardRegistration(set = "2X2", collectorNumber = "195")
@CardRegistration(set = "MOC", collectorNumber = "320")
public class ConclaveMentor extends Card {

    public ConclaveMentor() {
        addStaticEffect(new AddOnePlusOneCountersEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEqualToDyingSourcePowerEffect());
    }
}
