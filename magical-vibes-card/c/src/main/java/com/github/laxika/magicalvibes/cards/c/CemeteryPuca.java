package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "158")
public class CemeteryPuca extends Card {

    public CemeteryPuca() {
        // Whenever a creature dies, you may pay {1}. If you do, this creature becomes a copy
        // of that creature, except it has this ability.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new MayPayManaEffect("{1}",
                        new BecomeCopyOfDyingCreatureEffect(),
                        "Pay {1} to become a copy of that creature?"));
    }
}
