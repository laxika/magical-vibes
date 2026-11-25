package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "228")
public class Heartmender extends Card {

    public Heartmender() {
        // At the beginning of your upkeep, remove a -1/-1 counter from each creature you control.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RemoveCountersEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate()),
                CounterType.MINUS_ONE_MINUS_ONE, 1));

        // Persist is a keyword loaded from Scryfall; the engine handles the return automatically.
    }
}
