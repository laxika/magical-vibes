package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "115")
public class PatternOfRebirth extends Card {

    public PatternOfRebirth() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect());
    }
}
