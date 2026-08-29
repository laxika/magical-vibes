package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "31")
@CardRegistration(set = "ATH", collectorNumber = "21")
public class CuombajjWitches extends Card {

    public CuombajjWitches() {
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"
        );

        ActivatedAbility ability = new ActivatedAbility(
                true,
                null,
                List.of(
                        DealDamageToAnyTargetEffect.forTargetGroup(1, 0),
                        DealDamageToAnyTargetEffect.forTargetGroup(1, 1)
                ),
                "{T}: This creature deals 1 damage to any target and 1 damage to any target of an opponent's choice.",
                List.of(anyTarget, anyTarget),
                2,
                2
        );
        ability.withOpponentChosenTargetByController(1, anyTarget)
                .withAllowSharedTargets();
        addActivatedAbility(ability);
    }
}
