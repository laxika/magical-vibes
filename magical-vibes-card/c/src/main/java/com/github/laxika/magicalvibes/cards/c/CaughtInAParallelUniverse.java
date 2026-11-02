package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "572")
public class CaughtInAParallelUniverse extends Card {

    public CaughtInAParallelUniverse() {
        addTrigger(Triggers.encounterThis(),
                new EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect());
    }
}
