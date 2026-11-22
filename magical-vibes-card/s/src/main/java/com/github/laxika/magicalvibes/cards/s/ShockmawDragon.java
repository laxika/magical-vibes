package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "114")
public class ShockmawDragon extends Card {

    public ShockmawDragon() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToPermanentsTargetControlsEffect(1,
                        Subjects.controlledBy(Subjects.triggeringPlayer(), new PermanentIsCreaturePredicate())));
    }
}
