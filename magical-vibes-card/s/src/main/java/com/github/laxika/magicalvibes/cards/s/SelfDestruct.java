package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "157")
public class SelfDestruct extends Card {

    public SelfDestruct() {
        DealDamageEffect selfDamage =
                DealDamageEffect.targetCreatureToItself(1);
        DealDamageEffect targetDamage =
                DealDamageEffect.toAnyTargetInGroup(new SourcePower(), 1).dealtBy(Subjects.target(0));
        target(TargetFilters.creatureYouControl()).bindEffect(selfDamage);
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Second target must be any other target"
        )).bindEffect(targetDamage);
        addSimultaneousSpellEffects(selfDamage, targetDamage);
    }
}
