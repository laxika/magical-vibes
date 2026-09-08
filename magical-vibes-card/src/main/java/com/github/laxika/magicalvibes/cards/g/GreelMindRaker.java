package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "66")
public class GreelMindRaker extends Card {

    public GreelMindRaker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        new DiscardEffect(new XValue(), DiscardRecipient.TARGET_PLAYER, true)
                ),
                "{X}{B}, {T}, Discard two cards: Target player discards X cards at random.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
