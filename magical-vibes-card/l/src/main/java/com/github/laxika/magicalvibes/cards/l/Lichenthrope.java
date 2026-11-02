package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageWithMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "112")
public class Lichenthrope extends Card {

    public Lichenthrope() {
        // If damage would be dealt to this creature, put that many -1/-1 counters on it instead.
        addStaticEffect(new ReplaceDamageWithMinusOneCountersEffect());

        // At the beginning of your upkeep, remove a -1/-1 counter from this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
