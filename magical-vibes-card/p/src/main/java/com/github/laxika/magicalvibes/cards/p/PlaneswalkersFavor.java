package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "86")
public class PlaneswalkersFavor extends Card {

    public PlaneswalkersFavor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(RevealCardsFromHandEffect.atRandom(Subjects.targetPlayer(0), 1),
                        BoostEffect.targetInGroup(AmountThisWay.revealedManaValue(), AmountThisWay.revealedManaValue(), 1)),
                "{3}{G}: Target opponent reveals a card at random from their hand. Target creature gets +X/+X until end of turn, where X is the revealed card's mana value.",
                null,
                null,
                null,
                null,
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        TargetFilters.creature()),
                2,
                2));
    }
}
