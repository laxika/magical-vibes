package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndReattachAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "14")
public class GiftOfImmortality extends Card {

    public GiftOfImmortality() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnEnchantedCreatureAndReattachAuraOnDeathEffect());
    }
}
