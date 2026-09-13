package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "60")
@CardRegistration(set = "DD2", collectorNumber = "42")
@CardRegistration(set = "HOP", collectorNumber = "54")
@CardRegistration(set = "VMA", collectorNumber = "160")
@CardRegistration(set = "JVC", collectorNumber = "42")
public class FlametongueKavu extends Card {

    public FlametongueKavu() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(4));
    }
}
