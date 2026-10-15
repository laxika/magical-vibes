package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "ULG", collectorNumber = "27")
public class AuraFlux extends Card {

    public AuraFlux() {
        // Other enchantments have "At the beginning of your upkeep, sacrifice this enchantment
        // unless you pay {2}."
        addEffect(EffectSlot.STATIC,
                new AllPermanentsUpkeepSacrificeUnlessPayEffect(
                        new PermanentIsEnchantmentPredicate(), "{2}", true));
    }
}
