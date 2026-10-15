package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "SHM", collectorNumber = "158")
public class CemeteryPuca extends Card {

    public CemeteryPuca() {
        // Whenever a creature dies, you may pay {1}. If you do, this creature becomes a copy
        // of that creature, except it has this ability.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new MayPayManaEffect("{1}",
                        new BecomeCopyOfDyingCreatureEffect(),
                        "Pay {1} to become a copy of that creature?"));
    }
}
