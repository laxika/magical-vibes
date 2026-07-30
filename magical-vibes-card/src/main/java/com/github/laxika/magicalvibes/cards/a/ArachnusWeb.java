package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreaturePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "163")
public class ArachnusWeb extends Card {

    public ArachnusWeb() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature can't attack or block, and its activated abilities can't be activated.
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect())
                // At the beginning of the end step, if enchanted creature's power is 4 or greater,
                // destroy this Aura.
                .addEffect(EffectSlot.END_STEP_TRIGGERED,
                        new ConditionalEffect(new EnchantedCreaturePowerAtLeast(4),
                                new DestroySourcePermanentEffect()));
    }
}
