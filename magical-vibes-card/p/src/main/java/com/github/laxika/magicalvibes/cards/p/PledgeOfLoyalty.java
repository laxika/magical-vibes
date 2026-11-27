package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "24")
public class PledgeOfLoyalty extends Card {

    public PledgeOfLoyalty() {
        target(TargetFilters.creature())
                .addStaticEffect(ProtectionFromColorsEffect.amongPermanentsYouControl(Subjects.attachedCreature())
                        .withAuraRemoval(ProtectionFromColorsEffect.AuraRemoval.KEEPS_THIS_AURA));
    }
}
