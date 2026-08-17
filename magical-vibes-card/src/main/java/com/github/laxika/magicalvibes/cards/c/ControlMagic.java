package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "64")
@CardRegistration(set = "BRB", collectorNumber = "14")
public class ControlMagic extends Card {

    public ControlMagic() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
