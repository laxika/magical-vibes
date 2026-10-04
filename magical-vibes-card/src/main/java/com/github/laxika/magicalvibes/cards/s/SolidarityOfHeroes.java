package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "141")
@CardRegistration(set = "C16", collectorNumber = "168")
@CardRegistration(set = "CM2", collectorNumber = "144")
public class SolidarityOfHeroes extends Card {

    public SolidarityOfHeroes() {
        setAdditionalManaCostPerExtraTarget("{1}{G}");
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new DoublePlusOneCountersOnTargetCreatureEffect());
    }
}
