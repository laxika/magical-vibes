package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "183")
public class Electryte extends Card {

    public Electryte() {
        // "It deals damage equal to its power to each blocking creature": every blocking creature, whoever controls it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(
                        List.of(new PermanentIsCreaturePredicate(), new PermanentIsBlockingPredicate()))),
                        new SourcePower()));
    }
}
