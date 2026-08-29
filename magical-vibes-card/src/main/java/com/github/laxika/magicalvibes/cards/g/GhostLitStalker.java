package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "SOK", collectorNumber = "69")
public class GhostLitStalker extends Card {

    public GhostLitStalker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{B}",
                List.of(new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)),
                "{4}{B}, {T}: Target player discards two cards. Activate only as a sorcery.",
                anyPlayer(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{B}",
                List.of(new DiscardEffect(4, DiscardRecipient.TARGET_PLAYER)),
                "Channel — {5}{B}{B}, Discard this card: Target player discards four cards. Activate only as a sorcery.",
                anyPlayer(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
