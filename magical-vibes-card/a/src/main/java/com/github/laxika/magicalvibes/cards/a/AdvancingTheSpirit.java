package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstPowerUpFreeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "712")
public class AdvancingTheSpirit extends Card {

    public AdvancingTheSpirit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addStaticEffect(new FirstPowerUpFreeEffect());
    }
}
