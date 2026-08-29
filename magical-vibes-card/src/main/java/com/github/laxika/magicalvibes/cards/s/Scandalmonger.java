package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "158")
public class Scandalmonger extends Card {

    public Scandalmonger() {
        // {2}: Target player discards a card. Any player may activate this ability but only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{2}: Target player discards a card. Any player may activate this ability but only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivatableByAnyPlayer());
    }
}
