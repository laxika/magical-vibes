package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "210")
public class AzorsElocutors extends Card {

    public AzorsElocutors() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.FILIBUSTER),
                new ConditionalEffect(new SourceCounterThreshold(5, CounterType.FILIBUSTER), new WinGameEffect())));

        addTrigger(Triggers.youAreDealtDamage(),
                new RemoveCounterFromSourceEffect(CounterType.FILIBUSTER, 1));
    }
}
