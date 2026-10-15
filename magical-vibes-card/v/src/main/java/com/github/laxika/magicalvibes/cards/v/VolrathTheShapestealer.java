package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetPermanentUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "51")
public class VolrathTheShapestealer extends Card {

    public VolrathTheShapestealer() {
        PermanentPredicateTargetFilter creatureTarget = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature.");
        target(creatureTarget, 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BecomeCopyOfTargetPermanentUntilYourNextTurnEffect(
                        new PermanentIsCreaturePredicate(), 7, 5, true)),
                "{1}: Until your next turn, Volrath becomes a copy of target creature, except it's 7/5 and it has this ability.",
                creatureTarget));
    }
}
