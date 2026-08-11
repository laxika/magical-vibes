package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "24")
public class PledgeOfLoyalty extends Card {

    public PledgeOfLoyalty() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new ProtectionFromColorsOfPermanentsYouControlEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
