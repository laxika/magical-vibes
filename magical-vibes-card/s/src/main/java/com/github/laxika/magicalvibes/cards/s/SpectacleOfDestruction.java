package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "8")
public class SpectacleOfDestruction extends Card {

    public SpectacleOfDestruction() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY)), TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.WRECK));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.WRECK),
                        SequenceEffect.of(
                                new RemoveCounterFromSourceEffect(CounterType.WRECK, 1),
                                new SeekEffect(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
