package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "646")
public class WaspShrinkingSavior extends Card {

    public WaspShrinkingSavior() {
        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentPredicate creatureWithNegativePower = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(-1)
        ));

        var boostTargetCreature = BoostEffect.target(new Fixed(-3), new Fixed(0), otherCreature,
                        Duration.UNTIL_YOUR_NEXT_TURN);
        target(new PermanentPredicateTargetFilter(
                otherCreature,
                "Target must be another creature"
        ), 0, 1).bindEffect(boostTargetCreature);
        addTrigger(Triggers.attacks(),
                boostTargetCreature,
                new DrawCardEffect(
                new PermanentCount(creatureWithNegativePower, CountScope.ANY_PLAYER)));
    }
}
