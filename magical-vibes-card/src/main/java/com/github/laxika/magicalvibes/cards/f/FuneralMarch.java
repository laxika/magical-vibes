package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerSacrificesCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "164")
@CardRegistration(set = "HML", collectorNumber = "48")
public class FuneralMarch extends Card {

    public FuneralMarch() {
        // Enchant creature
        target(TargetFilters.creature())
        // When enchanted creature leaves the battlefield, its controller sacrifices a creature of their choice.
        .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD,
                new EnchantedControllerSacrificesCreatureOnLeaveEffect());
    }
}
