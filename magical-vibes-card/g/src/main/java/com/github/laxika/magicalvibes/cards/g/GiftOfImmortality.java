package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndReattachAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "14")
@CardRegistration(set = "SOC", collectorNumber = "147")
@CardRegistration(set = "MSC", collectorNumber = "134")
@CardRegistration(set = "MSC", collectorNumber = "306")
public class GiftOfImmortality extends Card {

    public GiftOfImmortality() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnEnchantedCreatureAndReattachAuraOnDeathEffect());
    }
}
