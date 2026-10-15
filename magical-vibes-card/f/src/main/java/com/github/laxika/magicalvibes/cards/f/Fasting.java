package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FastingEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DRK", collectorNumber = "7")
public class Fasting extends Card {

    public Fasting() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.HUNGER),
                new ConditionalEffect(new SourceCounterThreshold(5, CounterType.HUNGER),
                        new DestroyReferencedPermanentEffect(PermanentReference.SOURCE))));

        addEffect(EffectSlot.MAY_SKIP_DRAW_STEP_DRAW, new FastingEffect());

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new DestroyReferencedPermanentEffect(PermanentReference.SOURCE));
    }
}
