package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "52")
@CardRegistration(set = "ICE", collectorNumber = "54")
@CardRegistration(set = "ATH", collectorNumber = "16")
@CardRegistration(set = "BRB", collectorNumber = "84")
@CardRegistration(set = "SUM", collectorNumber = "41")
@CardRegistration(set = "CST", collectorNumber = "54")
@CardRegistration(set = "3ED", collectorNumber = "41")
@CardRegistration(set = "ME2", collectorNumber = "37")
@CardRegistration(set = "DDF", collectorNumber = "22")
@CardRegistration(set = "VMA", collectorNumber = "51")
@CardRegistration(set = "V13", collectorNumber = "2")
public class SwordsToPlowshares extends Card {

    public SwordsToPlowshares() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect());
    }
}
