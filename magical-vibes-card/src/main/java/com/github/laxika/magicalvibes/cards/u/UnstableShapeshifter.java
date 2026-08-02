package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureEffect;

@CardRegistration(set = "TMP", collectorNumber = "100")
public class UnstableShapeshifter extends Card {

    public UnstableShapeshifter() {
        // Whenever another creature enters, this creature becomes a copy of that creature,
        // except it has this ability.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new BecomeCopyOfEnteringCreatureEffect());
    }
}
