package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "71")
public class CephalidBroker extends Card {

    public CephalidBroker() {
        // {T}: Target player draws two cards, then discards two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DrawCardForTargetPlayerEffect(2),
                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)
                ),
                "{T}: Target player draws two cards, then discards two cards.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
