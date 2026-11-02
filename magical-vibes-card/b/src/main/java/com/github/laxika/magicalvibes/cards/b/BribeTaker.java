package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterForEachControlledCounterKindEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "55")
@CardRegistration(set = "NCC", collectorNumber = "155")
public class BribeTaker extends Card {

    public BribeTaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCounterForEachControlledCounterKindEffect());
    }
}
