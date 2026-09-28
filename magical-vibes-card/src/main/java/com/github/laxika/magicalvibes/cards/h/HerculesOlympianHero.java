package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSC", collectorNumber = "16")
@CardRegistration(set = "MSC", collectorNumber = "308")
public class HerculesOlympianHero extends Card {

    public HerculesOlympianHero() {
        // Whenever Hercules attacks, put a +1/+1 counter on him. He gains indestructible until
        // end of turn.
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        // Whenever Hercules is dealt damage for the first time each turn, put that many +1/+1
        // counters on him. The damage amount is captured on the trigger's event value.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new OncePerTurnTriggerEffect(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())));
    }
}
