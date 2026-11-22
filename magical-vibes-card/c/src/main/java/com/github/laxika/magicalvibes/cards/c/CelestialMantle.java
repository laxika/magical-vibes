package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "6")
public class CelestialMantle extends Card {

    public CelestialMantle() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()))
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new DoubleControllerLifeEffect(Subjects.controllerOf(Subjects.triggering())));
    }
}
