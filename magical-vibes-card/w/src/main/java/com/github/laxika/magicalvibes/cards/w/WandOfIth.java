package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "114")
public class WandOfIth extends Card {

    public WandOfIth() {
        // {3}, {T}: Target player reveals a card at random from their hand. If it's a land card,
        // that player discards it unless they pay 1 life. Otherwise, they discard it unless they
        // pay life equal to its mana value. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect()),
                "{3}, {T}: Target player reveals a card at random from their hand. If it's a land card, "
                        + "that player discards it unless they pay 1 life. If it isn't a land card, "
                        + "the player discards it unless they pay life equal to its mana value. "
                        + "Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
