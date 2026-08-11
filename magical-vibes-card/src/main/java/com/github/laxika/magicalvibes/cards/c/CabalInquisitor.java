package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "119")
public class CabalInquisitor extends Card {

    public CabalInquisitor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)
                ),
                "Threshold — {1}{B}, {T}, Exile two cards from your graveyard: Target player discards a card. "
                        + "Activate only as a sorcery and only if there are seven or more cards in your graveyard.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new GraveyardCardThreshold(7, null),
                "Activate only if there are seven or more cards in your graveyard."
        ));
    }
}
