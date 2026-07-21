package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "179")
public class IfnirDeadlands extends Card {

    public IfnirDeadlands() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}, Pay 1 life: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.BLACK)),
                "{T}, Pay 1 life: Add {B}."
        ));

        // {2}{B}{B}, {T}, Sacrifice a Desert: Put two -1/-1 counters on target creature an
        // opponent controls. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                                "Sacrifice a Desert",
                                false),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 2)),
                "{2}{B}{B}, {T}, Sacrifice a Desert: Put two -1/-1 counters on target creature an "
                        + "opponent controls. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(
                                        new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a creature an opponent controls"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
