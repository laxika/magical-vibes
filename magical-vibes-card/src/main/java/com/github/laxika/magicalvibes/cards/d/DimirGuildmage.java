package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "245")
public class DimirGuildmage extends Card {

    public DimirGuildmage() {
        // {3}{U}: Target player draws a card. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new DrawCardForTargetPlayerEffect(1, false, true)),
                "{3}{U}: Target player draws a card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        // {3}{B}: Target player discards a card. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{3}{B}: Target player discards a card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
