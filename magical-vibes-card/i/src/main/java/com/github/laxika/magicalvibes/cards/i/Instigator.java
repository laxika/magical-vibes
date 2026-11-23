package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "140")
public class Instigator extends Card {

    public Instigator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        MatchingCreaturesMustAttackThisTurnEffect.controlledBy(Subjects.targetPlayer())
                ),
                "{1}{B}{B}, {T}, Discard a card: Creatures target player controls attack this turn if able.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
