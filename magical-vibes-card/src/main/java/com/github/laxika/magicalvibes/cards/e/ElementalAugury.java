package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "286")
public class ElementalAugury extends Card {

    public ElementalAugury() {
        // {3}: Look at the top three cards of target player's library, then put them back in any
        // order. The controller sees the cards privately and decides the new order.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ReorderTopCardsOfTargetLibraryEffect(3)),
                "{3}: Look at the top three cards of target player's library, then put them back in any order.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")));
    }
}
