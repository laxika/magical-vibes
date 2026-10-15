package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "289")
public class WingsOfHope extends Card {

    public WingsOfHope() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 3, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));
    }
}
