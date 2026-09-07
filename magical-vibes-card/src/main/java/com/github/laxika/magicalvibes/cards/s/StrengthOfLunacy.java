package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "86")
public class StrengthOfLunacy extends Card {

    public StrengthOfLunacy() {
        addCastingOption(new MadnessCast("{B}"));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                        Set.of(CardColor.WHITE), GrantScope.ENCHANTED_CREATURE));
    }
}
