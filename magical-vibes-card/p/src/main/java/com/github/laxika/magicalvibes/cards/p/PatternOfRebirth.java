package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "115")
@CardRegistration(set = "UMA", collectorNumber = "176")
public class PatternOfRebirth extends Card {

    public PatternOfRebirth() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect());
    }
}
