package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M10", collectorNumber = "63")
@CardRegistration(set = "M11", collectorNumber = "67")
@CardRegistration(set = "M12", collectorNumber = "67")
public class MindControl extends Card {

    public MindControl() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
