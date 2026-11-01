package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "100")
public class UnstableShapeshifter extends Card {

    public UnstableShapeshifter() {
        // Whenever another creature enters, this creature becomes a copy of that creature,
        // except it has this ability.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new BecomeCopyOfEnteringCreatureEffect());
    }
}
