package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "133")
public class MyrMoonvessel extends Card {

    public MyrMoonvessel() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new AwardManaEffect(ManaColor.COLORLESS));
    }
}
