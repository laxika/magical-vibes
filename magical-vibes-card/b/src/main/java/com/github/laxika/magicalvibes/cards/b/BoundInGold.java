package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantCrewEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KHM", collectorNumber = "5")
public class BoundInGold extends Card {

    public BoundInGold() {
        target(TargetFilters.permanent())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
                .addStaticEffect(new EnchantedCreatureCantCrewEffect())
                .addStaticEffect(new EnchantedPermanentCantActivateAbilitiesEffect());
    }
}
