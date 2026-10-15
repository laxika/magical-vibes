package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "214")
public class BoltwingMarauder extends Card {

    public BoltwingMarauder() {
        // Whenever another creature you control enters, target creature gets +2/+0 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new BoostTargetCreatureEffect(2, 0));
    }
}
