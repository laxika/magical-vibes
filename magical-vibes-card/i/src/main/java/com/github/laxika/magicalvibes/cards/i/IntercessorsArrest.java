package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantCrewEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEO", collectorNumber = "20")
public class IntercessorsArrest extends Card {

    public IntercessorsArrest() {
        target(TargetFilters.permanent())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
                .addStaticEffect(new EnchantedCreatureCantCrewEffect())
                .addStaticEffect(new EnchantedPermanentCantActivateAbilitiesEffect());
    }
}
