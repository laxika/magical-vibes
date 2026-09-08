package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentPower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "169")
public class UnnaturalHunger extends Card {

    public UnnaturalHunger() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new SacrificeOtherCreatureOrDamageEffect(
                                new EnchantedPermanentPower(),
                                DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
