package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "144")
@CardRegistration(set = "SCD", collectorNumber = "153")
public class ProvokeTheTrolls extends Card {

    public ProvokeTheTrolls() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target")).addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addSpellEffect(new BoostSelfEffect(
                Subjects.thisWay(StepKind.DEAL_DAMAGE, new PermanentIsCreaturePredicate()), 5, 0));
    }
}
