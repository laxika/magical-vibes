package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "17")
public class StrengthOfIsolation extends Card {

    public StrengthOfIsolation() {
        addCastingOption(new MadnessCast("{W}"));
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new ProtectionFromColorsEffect(
                        Set.of(CardColor.BLACK), GrantScope.ENCHANTED_CREATURE));
    }
}
