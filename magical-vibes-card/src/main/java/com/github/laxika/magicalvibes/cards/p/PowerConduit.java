package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/** Implements Power Conduit's two target-specific modes as separate activated abilities. */
@CardRegistration(set = "MRD", collectorNumber = "229")
public class PowerConduit extends Card {

    public PowerConduit() {
        // The printed ability is modal, but its modes have different target types. The engine's
        // activated-ability modal wrapper cannot carry per-mode target filters, so each mode is
        // represented as its own ability with the shared printed cost.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.CHARGE)
                ),
                "{T}, Remove a counter from a permanent you control: Put a charge counter on target artifact.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact."
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{T}, Remove a counter from a permanent you control: Put a +1/+1 counter on target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature."
                )
        ));
    }
}
