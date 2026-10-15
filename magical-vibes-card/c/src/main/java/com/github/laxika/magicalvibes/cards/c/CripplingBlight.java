package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M13", collectorNumber = "85")
@CardRegistration(set = "M15", collectorNumber = "92")
public class CripplingBlight extends Card {

    public CripplingBlight() {
        // Enchant creature
        target(TargetFilters.creature());

        // Enchanted creature gets -1/-1
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_CREATURE));

        // Enchanted creature can't block
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
