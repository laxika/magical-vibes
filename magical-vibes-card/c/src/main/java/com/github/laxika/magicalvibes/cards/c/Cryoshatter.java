package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "53")
public class Cryoshatter extends Card {

    public Cryoshatter() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-5, 0, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                        new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED))
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}
