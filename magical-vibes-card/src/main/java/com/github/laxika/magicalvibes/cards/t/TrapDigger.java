package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "24")
public class TrapDigger extends Card {

    public TrapDigger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.TRAP, 1, new PermanentIsLandPredicate())),
                "{2}{W}, {T}: Put a trap counter on target land you control.",
                TargetFilters.landYouControl()
        ));

        PermanentPredicate landWithTrapCounter = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasCountersPredicate(CounterType.TRAP)
        ));
        PermanentPredicate attackingCreatureWithoutFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new SacrificePermanentCost(landWithTrapCounter,
                                "Sacrifice a land with a trap counter on it", false),
                        new DealDamageToTargetCreatureEffect(3, attackingCreatureWithoutFlying)
                ),
                "Sacrifice a land with a trap counter on it: This creature deals 3 damage to target attacking creature without flying.",
                new PermanentPredicateTargetFilter(
                        attackingCreatureWithoutFlying,
                        "Target must be an attacking creature without flying"
                )
        ));
    }
}
