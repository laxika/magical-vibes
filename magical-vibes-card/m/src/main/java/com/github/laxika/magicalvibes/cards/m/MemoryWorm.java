package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "90")
@CardRegistration(set = "WHO", collectorNumber = "695")
public class MemoryWorm extends Card {

    public MemoryWorm() {
        // Paradox — Whenever you cast a spell from anywhere other than your hand, this creature
        // deals 2 damage to target player. That player discards a card, then draws a card. Put a
        // +1/+1 counter on this creature.
        List<CardEffect> paradox = List.of(
                DealDamageEffect.to(Subjects.targetPlayer(), 2),
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.targetPlayer()),
                        new DrawCardEffect(1, Subjects.targetPlayerDeclaredElsewhere())),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player")).addTrigger(Triggers.youCastSpell()
                        .notCastFrom(Zone.HAND),
                paradox.toArray(CardEffect[]::new));
    }
}
