package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "113")
public class DrannithStinger extends Card {

    public DrannithStinger() {
        addTrigger(Triggers.youCycleCard(),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
        addCycling("{1}");
    }
}
