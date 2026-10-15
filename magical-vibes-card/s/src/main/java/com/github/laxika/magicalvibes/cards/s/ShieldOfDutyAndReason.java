package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "16")
public class ShieldOfDutyAndReason extends Card {

    public ShieldOfDutyAndReason() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC,
                new ProtectionFromColorsEffect(Set.of(CardColor.GREEN, CardColor.BLUE), GrantScope.ENCHANTED_CREATURE));
    }
}
