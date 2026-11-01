package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingSourcePowerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "222")
public class BottleGolems extends Card {

    public BottleGolems() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEqualToDyingSourcePowerEffect());
    }
}
