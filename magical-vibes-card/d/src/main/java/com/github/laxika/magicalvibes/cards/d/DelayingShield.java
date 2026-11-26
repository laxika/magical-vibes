package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageToControllerCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ODY", collectorNumber = "17")
public class DelayingShield extends Card {

    public DelayingShield() {
        addStaticEffect(new DamageToControllerCounterReplacementEffect(CounterType.DELAY));
        // At the beginning of your upkeep, remove all delay counters from this enchantment. For each delay counter
        // removed this way, you lose 1 life unless you pay {1}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new RemoveAllCountersEffect(CounterType.DELAY),
                new RepeatEffect(new EventValue(), UnlessPaysEffect.unlessYouPay("{1}{W}", new LoseLifeEffect(1, Subjects.you())))));
    }
}
