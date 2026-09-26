package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "64")
@CardRegistration(set = "BRB", collectorNumber = "14")
@CardRegistration(set = "SUM", collectorNumber = "52")
@CardRegistration(set = "3ED", collectorNumber = "52")
@CardRegistration(set = "VMA", collectorNumber = "63")
@CardRegistration(set = "DDM", collectorNumber = "30")
@CardRegistration(set = "EMA", collectorNumber = "42")
@CardRegistration(set = "SLD", collectorNumber = "2151")
@CardRegistration(set = "AA2", collectorNumber = "3")
@CardRegistration(set = "ME4", collectorNumber = "43")
@CardRegistration(set = "2ED", collectorNumber = "53")
@CardRegistration(set = "C13", collectorNumber = "35")
public class ControlMagic extends Card {

    public ControlMagic() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
