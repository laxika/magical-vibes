package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "65")
public class CloakOfMists extends Card {

    public CloakOfMists() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
