package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "8ED", collectorNumber = "103")
@CardRegistration(set = "7ED", collectorNumber = "99")
@CardRegistration(set = "5ED", collectorNumber = "128")
@CardRegistration(set = "4ED", collectorNumber = "105")
@CardRegistration(set = "SUM", collectorNumber = "84")
public class StealArtifact extends Card {

    public StealArtifact() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
