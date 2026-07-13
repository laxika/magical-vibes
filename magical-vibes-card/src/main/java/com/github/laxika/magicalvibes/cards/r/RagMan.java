package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndRandomDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "151")
@CardRegistration(set = "7ED", collectorNumber = "156")
public class RagMan extends Card {

    public RagMan() {
        // {B}{B}{B}, {T}: Target opponent reveals their hand and discards a creature card at random.
        // Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{B}{B}{B}",
                List.of(new RevealHandAndRandomDiscardCardTypeEffect(CardType.CREATURE)),
                "{B}{B}{B}, {T}: Target opponent reveals their hand and discards a creature card at random. "
                        + "Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
