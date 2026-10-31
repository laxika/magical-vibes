package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;


@CardRegistration(set = "ALA", collectorNumber = "83")
public class ResoundingScream extends Card {

    public ResoundingScream() {
        // Target player discards a card at random.
        // When you cycle this card, target player discards two cards at random. The reflexive cycle
        // trigger goes on the stack above the cycling draw, so it resolves first.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addSpellEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true))
                .addEffect(EffectSlot.ON_SELF_CYCLED, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER, true));

        // Cycling {5}{U}{B}{R} ({5}{U}{B}{R}, Discard this card: Draw a card.)
        addCycling("{5}{U}{B}{R}");
    }
}
