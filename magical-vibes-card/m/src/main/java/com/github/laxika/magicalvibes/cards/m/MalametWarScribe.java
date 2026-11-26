package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "21")
public class MalametWarScribe extends Card {

    public MalametWarScribe() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), BoostEffect.creaturesYouControl(2, 1));
    }
}
