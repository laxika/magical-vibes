package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "111")
public class UnflinchingCourage extends Card {

    public UnflinchingCourage() {
        target(TargetFilters.creature())
                // Enchanted creature gets +2/+2 and has trample and lifelink.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        2, 2, Set.of(Keyword.TRAMPLE, Keyword.LIFELINK), GrantScope.ENCHANTED_CREATURE));
    }
}
