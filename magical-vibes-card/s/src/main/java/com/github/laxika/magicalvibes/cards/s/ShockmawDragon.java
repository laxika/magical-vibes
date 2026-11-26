package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "114")
public class ShockmawDragon extends Card {

    public ShockmawDragon() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                DealDamageEffect.toEach(
                        Subjects.controlledBy(Subjects.triggeringPlayer(), new PermanentIsCreaturePredicate()),
                        1));
    }
}
