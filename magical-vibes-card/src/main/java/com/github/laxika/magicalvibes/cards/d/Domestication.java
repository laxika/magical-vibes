package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreaturePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "53")
@CardRegistration(set = "ROE", collectorNumber = "61")
public class Domestication extends Card {

    public Domestication() {
        // Enchant creature
        target(TargetFilters.creature())
                // You control enchanted creature.
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect())
                // At the beginning of your end step, if enchanted creature's power is 4 or greater,
                // sacrifice this Aura.
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new ConditionalEffect(new EnchantedCreaturePowerAtLeast(4),
                                new SacrificeSelfEffect()));
    }
}
