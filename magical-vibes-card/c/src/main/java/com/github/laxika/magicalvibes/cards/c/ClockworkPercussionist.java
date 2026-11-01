package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "130")
public class ClockworkPercussionist extends Card {

    public ClockworkPercussionist() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
