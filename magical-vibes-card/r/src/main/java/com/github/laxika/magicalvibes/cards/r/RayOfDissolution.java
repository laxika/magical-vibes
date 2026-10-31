package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "27")
public class RayOfDissolution extends Card {

    public RayOfDissolution() {
        target(TargetFilters.enchantment())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(3));
    }
}
