package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "53")
public class Cryoshatter extends Card {

    public Cryoshatter() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(-5, 0, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                        new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED))
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE,
                        new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));
    }
}
