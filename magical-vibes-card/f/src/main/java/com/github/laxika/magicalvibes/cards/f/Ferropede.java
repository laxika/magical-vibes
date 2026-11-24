package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "122")
public class Ferropede extends Card {

    public Ferropede() {
        addStaticEffect(new CantBeBlockedEffect());

        target(TargetFilters.permanent()).addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(RemoveUpToCountersFromAllPermanentsEffect.exactly(CounterType.ANY, new Fixed(1), Subjects.target()),
                        "Remove a counter from target permanent?"));
    }
}
