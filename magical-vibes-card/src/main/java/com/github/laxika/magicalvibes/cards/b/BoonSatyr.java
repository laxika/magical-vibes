package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "152")
public class BoonSatyr extends Card {

    public BoonSatyr() {
        addCastingOption(new BestowCast("{3}{G}{G}"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 2, GrantScope.ENCHANTED_CREATURE));
    }
}
