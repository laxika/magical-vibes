package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "58")
public class MarkOfEviction extends Card {

    public MarkOfEviction() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, ReturnToHandEffect.enchantedAndAuras());
    }
}
