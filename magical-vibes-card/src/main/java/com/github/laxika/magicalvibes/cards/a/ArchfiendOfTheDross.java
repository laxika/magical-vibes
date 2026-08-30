package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ONE", collectorNumber = "82")
public class ArchfiendOfTheDross extends Card {

    public ArchfiendOfTheDross() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new Fixed(4)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new RemoveCounterFromSourceEffect(CounterType.OIL, 1),
                new ConditionalEffect(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.OIL)),
                        new ControllerLosesGameEffect())));

        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
