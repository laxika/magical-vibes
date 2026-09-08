package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PCY", collectorNumber = "65")
public class Flay extends Card {

    public Flay() {
        // Target player discards a card at random, then may pay {1} to avoid another random discard.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true))
                .addEffect(EffectSlot.SPELL, new MayPayManaEffect(
                        "{1}",
                        null,
                        "Pay {1} to avoid discarding another card?",
                        MayPayPayer.TRIGGERING_PLAYER,
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true),
                        0));
    }
}
