package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "125")
public class MolderBeast extends Card {

    public MolderBeast() {
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY), BoostEffect.self(2, 0));
    }
}
