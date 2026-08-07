package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

/**
 * Roc Hatchling — {R} Creature — Bird 0/1.
 * <p>
 * A four-turn clock: it enters with four shell counters, sheds one at each of its
 * controller's upkeeps, and once the shell is gone it becomes a 3/3 flier.
 */
@CardRegistration(set = "WTH", collectorNumber = "113")
public class RocHatchling extends Card {

    public RocHatchling() {
        // This creature enters with four shell counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.SHELL, new Fixed(4)));

        // At the beginning of your upkeep, remove a shell counter from this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterFromSourceEffect(CounterType.SHELL, 1));

        // As long as this creature has no shell counters on it, it gets +3/+2 and has flying.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.SHELL)),
                new StaticBoostEffect(3, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.SHELL)),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
