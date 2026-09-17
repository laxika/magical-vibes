package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "89")
public class BucketList extends Card {

    public BucketList() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, triggerFor(CardType.ARTIFACT, CounterType.ARTIFACT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, triggerFor(CardType.CREATURE, CounterType.CREATURE));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, triggerFor(CardType.ENCHANTMENT, CounterType.ENCHANTMENT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, triggerFor(CardType.INSTANT, CounterType.INSTANT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, triggerFor(CardType.SORCERY, CounterType.SORCERY));
    }

    private static SpellCastTriggerEffect triggerFor(CardType cardType, CounterType counterType) {
        CardEffect markAndDraw = ConditionalEffect.unless(
                new NotCondition(new SourceCounterThreshold(1, counterType)),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(counterType),
                        new DrawCardEffect()));
        CardEffect complete = new ConditionalEffect(
                new AllConditions(List.of(
                        new SourceCounterThreshold(1, CounterType.ARTIFACT),
                        new SourceCounterThreshold(1, CounterType.CREATURE),
                        new SourceCounterThreshold(1, CounterType.ENCHANTMENT),
                        new SourceCounterThreshold(1, CounterType.INSTANT),
                        new SourceCounterThreshold(1, CounterType.SORCERY))),
                SequenceEffect.of(new SacrificeSelfEffect(), new DrawCardEffect()));
        return new SpellCastTriggerEffect(new CardTypePredicate(cardType), List.of(markAndDraw, complete));
    }
}
