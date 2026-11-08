package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "104")
public class MireBlight extends Card {

    public MireBlight() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}
