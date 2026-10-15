package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "KHC", collectorNumber = "4")
public class HeroOfBretagard extends Card {

    public HeroOfBretagard() {
        PutCountersOnSelfEffect counters = new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new EventValue());
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_EXILED_FROM_HAND, counters);
        addEffect(EffectSlot.ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT, counters);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantSubtypeEffect(CardSubtype.ANGEL, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantSubtypeEffect(CardSubtype.GOD, GrantScope.SELF)));
    }
}
