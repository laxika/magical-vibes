package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OneOrMoreCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YDFT", collectorNumber = "8")
public class SpectacleOfDestruction extends Card {

    public SpectacleOfDestruction() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.WRECK)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.WRECK),
                        SequenceEffect.of(
                                new RemoveCounterFromSourceEffect(CounterType.WRECK, 1),
                                new SeekLibraryToHandEffect(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
