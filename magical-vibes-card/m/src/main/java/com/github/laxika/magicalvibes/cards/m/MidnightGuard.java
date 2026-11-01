package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "14")
@CardRegistration(set = "M15", collectorNumber = "20")
public class MidnightGuard extends Card {

    public MidnightGuard() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
