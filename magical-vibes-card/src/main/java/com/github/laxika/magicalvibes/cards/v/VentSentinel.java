package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "171")
public class VentSentinel extends Card {

    public VentSentinel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                        new PermanentCount(
                                new PermanentHasKeywordPredicate(Keyword.DEFENDER), CountScope.CONTROLLER))),
                "{1}{R}, {T}: This creature deals damage to target player or planeswalker equal to the number of creatures you control with defender.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player or planeswalker")));
    }
}
