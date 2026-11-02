package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "46")
@CardRegistration(set = "KLR", collectorNumber = "67")
@CardRegistration(set = "NCC", collectorNumber = "232")
public class SkyshipPlunderer extends Card {

    public SkyshipPlunderer() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new AddAnotherCounterOfEachKindToTargetEffect());
    }
}
