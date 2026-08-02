package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

@CardRegistration(set = "RTR", collectorNumber = "210")
public class AzorsElocutors extends Card {

    public AzorsElocutors() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.FILIBUSTER),
                new ConditionalEffect(new SourceCounterThreshold(5, CounterType.FILIBUSTER), new WinGameEffect())));

        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE,
                new RemoveCounterFromSourceEffect(CounterType.FILIBUSTER, 1));
    }
}
