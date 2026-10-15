package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "93")
public class FracturedLoyalty extends Card {

    public FracturedLoyalty() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                        new TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect(
                                ControlDuration.PERMANENT));
    }
}
