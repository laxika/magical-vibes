package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "191")
public class HostileTakeover extends Card {

    public HostileTakeover() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(1, 1));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(4, 4));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));
    }
}
