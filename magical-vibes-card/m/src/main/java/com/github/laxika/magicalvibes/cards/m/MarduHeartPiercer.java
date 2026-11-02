package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "116")
@CardRegistration(set = "DDN", collectorNumber = "13")
public class MarduHeartPiercer extends Card {

    public MarduHeartPiercer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new Raid(), new DealDamageToAnyTargetEffect(2)));
    }
}
