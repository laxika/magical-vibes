package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "133")
public class VanishIntoMemory extends Card {

    public VanishIntoMemory() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffect());
    }
}
