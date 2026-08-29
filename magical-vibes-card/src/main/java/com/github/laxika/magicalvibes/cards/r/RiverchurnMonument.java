package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "57")
public class RiverchurnMonument extends Card {

    public RiverchurnMonument() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)),
                "{1}, {T}: Any number of target players each mill two cards.",
                List.of(anyPlayer()),
                0,
                99
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}{U}",
                List.of(new MillEffect(
                        new CardsInGraveyard(null, CountScope.TARGET_PLAYER),
                        MillRecipient.TARGET_PLAYER
                )),
                "Exhaust — {2}{U}{U}, {T}: Any number of target players each mill cards equal to the number of cards in their graveyard.",
                List.of(anyPlayer()),
                0,
                99
        ).withMaxActivationsPerGame(1).withExhaust());
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
