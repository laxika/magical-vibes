package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "UDS", collectorNumber = "11")
public class MaskOfLawAndGrace extends Card {

    public MaskOfLawAndGrace() {
        target(TargetFilters.creature()).addStaticEffect(new ProtectionFromColorsEffect(
                Set.of(CardColor.BLACK, CardColor.RED), GrantScope.ENCHANTED_CREATURE));
    }
}
