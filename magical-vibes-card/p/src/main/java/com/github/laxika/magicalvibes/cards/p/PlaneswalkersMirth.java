package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "12")
public class PlaneswalkersMirth extends Card {

    public PlaneswalkersMirth() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new RevealRandomCardFromTargetPlayerHandGainLifeEqualToManaValueEffect()),
                "{3}{W}: Target opponent reveals a card at random from their hand. You gain life equal to that card's mana value.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
