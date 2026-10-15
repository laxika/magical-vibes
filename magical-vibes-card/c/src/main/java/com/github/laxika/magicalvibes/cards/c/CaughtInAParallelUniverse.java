package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect;

@CardRegistration(set = "WHO", collectorNumber = "572")
public class CaughtInAParallelUniverse extends Card {

    public CaughtInAParallelUniverse() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED,
                new EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect());
    }
}
