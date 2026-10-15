package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "203")
public class ShipwreckSinger extends Card {

    public ShipwreckSinger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK)),
                "{1}{U}: Target creature an opponent controls attacks this turn if able.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a creature an opponent controls")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new BoostAllCreaturesEffect(-1, -1, new PermanentIsAttackingPredicate())),
                "{1}{B}, {T}: Attacking creatures get -1/-1 until end of turn."));
    }
}
