package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "136")
public class PurphorossEmissary extends Card {

    public PurphorossEmissary() {
        addCastingOption(new BestowCast("{6}{R}"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        3, 3, Set.of(Keyword.MENACE), GrantScope.ENCHANTED_CREATURE));
    }
}
