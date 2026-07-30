package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "58")
public class JaceMemoryAdept extends Card {

    public JaceMemoryAdept() {
        // +1: Draw a card. Target player mills a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "+1: Draw a card. Target player mills a card."
        ));

        // 0: Target player mills ten cards.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new MillEffect(10, MillRecipient.TARGET_PLAYER)),
                "0: Target player mills ten cards."
        ));

        // −7: Any number of target players each draw twenty cards.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new DrawCardForTargetPlayerEffect(20)),
                "−7: Any number of target players each draw twenty cards.",
                null, -7, null, null,
                List.of(anyPlayer(), anyPlayer()),
                0, 2
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
