package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "156")
public class OnakkeJavelineer extends Card {

    public OnakkeJavelineer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(2)),
                "{T}: This creature deals 2 damage to target player or battle.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsBattlePredicate(),
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player or battle")));
    }
}
