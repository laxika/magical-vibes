package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WHO", collectorNumber = "151")
@CardRegistration(set = "WHO", collectorNumber = "756")
@CardRegistration(set = "WHO", collectorNumber = "435")
@CardRegistration(set = "WHO", collectorNumber = "1026")
public class RegenerationsRestored extends Card {

    public RegenerationsRestored() {
        // This enchantment enters with twelve time counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(12)));

        // Vanishing: the upkeep removal and the last-counter sacrifice are separate triggers. The
        // sacrifice is added first so the card's own exile trigger sits above it on the stack and
        // can exile the enchantment (and earn the extra turn) before it is sacrificed.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceCounterThreshold(1, CounterType.TIME),
                        new RemoveCounterFromSourceEffect(CounterType.TIME, 1)));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTERS_REMOVED,
                new ConditionalEffect(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new SacrificeSelfEffect()));

        // Whenever one or more time counters are removed from this enchantment, scry 1 and gain 1
        // life. Then, if it has no time counters, exile it and take an extra turn.
        addEffect(EffectSlot.ON_SELF_TIME_COUNTERS_REMOVED, SequenceEffect.of(
                new ScryEffect(1),
                new GainLifeEffect(1),
                ConditionalEffect.unless(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new ExileSelfThenEffect(new ControllerExtraTurnEffect(1)))));
    }
}
