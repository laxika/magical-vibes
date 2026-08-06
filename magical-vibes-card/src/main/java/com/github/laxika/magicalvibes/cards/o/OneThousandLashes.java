package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "183")
public class OneThousandLashes extends Card {

    public OneThousandLashes() {
        target(TargetFilters.creature())
                // Enchanted creature can't attack or block, and its activated abilities
                // can't be activated.
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect())

                // At the beginning of the upkeep of enchanted creature's controller,
                // that player loses 1 life.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new EnchantedCreatureControllerLosesLifeEffect(1));
    }
}
