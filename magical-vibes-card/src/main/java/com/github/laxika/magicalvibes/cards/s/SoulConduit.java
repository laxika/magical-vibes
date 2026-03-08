package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "158")
public class SoulConduit extends Card {

    public SoulConduit() {
        addActivatedAbility(new ActivatedAbility(
                true, "{6}",
                List.of(new ExchangeTargetPlayersLifeTotalsEffect()),
                "{6}, {T}: Two target players exchange life totals.",
                List.of(
                        new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"),
                        new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player")
                ),
                2, 2
        ));
    }
}
