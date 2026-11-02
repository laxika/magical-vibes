package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesPermanentsOrCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "97")
public class DescentIntoMadness extends Card {

    public DescentIntoMadness() {
        // One ability, not two: the exile count reads the despair counter this trigger just added,
        // so the counter and the exiles must resolve inside a single stack entry.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.DESPAIR),
                new EachPlayerExilesPermanentsOrCardsFromHandEffect(
                        new CountersOnSource(CounterType.DESPAIR))));
    }
}
