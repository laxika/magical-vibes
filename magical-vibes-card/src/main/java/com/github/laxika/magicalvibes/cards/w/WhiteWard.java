package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "57")
@CardRegistration(set = "SUM", collectorNumber = "45")
public class WhiteWard extends Card {

    public WhiteWard() {
        // Enchant creature; enchanted creature has protection from white.
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                Set.of(CardColor.WHITE), GrantScope.ENCHANTED_CREATURE));
    }
}
