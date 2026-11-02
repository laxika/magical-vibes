package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "122")
public class Ferropede extends Card {

    public Ferropede() {
        addStaticEffect(new CantBeBlockedEffect());

        target(TargetFilters.permanent()).addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new RemoveChosenCountersFromTargetPermanentEffect(1),
                        "Remove a counter from target permanent?"));
    }
}
