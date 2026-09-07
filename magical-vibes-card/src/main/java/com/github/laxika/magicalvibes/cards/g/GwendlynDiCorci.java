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

@CardRegistration(set = "LEG", collectorNumber = "228")
public class GwendlynDiCorci extends Card {

    public GwendlynDiCorci() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true)),
                "{T}: Target player discards a card at random. Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
