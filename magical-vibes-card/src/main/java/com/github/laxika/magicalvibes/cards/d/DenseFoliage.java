package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

@CardRegistration(set = "6ED", collectorNumber = "221")
public class DenseFoliage extends Card {

    public DenseFoliage() {
        // Creatures can't be the targets of spells (abilities can still target them).
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(TargetingRestrictionEffect.spells(), GrantScope.ALL_CREATURES));
    }
}
