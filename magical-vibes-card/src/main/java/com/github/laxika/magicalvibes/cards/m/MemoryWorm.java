package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "90")
public class MemoryWorm extends Card {

    public MemoryWorm() {
        // Paradox — Whenever you cast a spell from anywhere other than your hand, this creature
        // deals 2 damage to target player. That player discards a card, then draws a card. Put a
        // +1/+1 counter on this creature.
        List<CardEffect> paradox = List.of(
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER),
                SequenceEffect.of(
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        new DrawCardForTargetPlayerEffect(1)),
                new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(paradox, new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")));
    }
}
