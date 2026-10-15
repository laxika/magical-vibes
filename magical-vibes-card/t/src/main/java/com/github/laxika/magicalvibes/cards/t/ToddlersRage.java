package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "331")
@CardRegistration(set = "MB2", collectorNumber = "568")
public class ToddlersRage extends Card {

    public ToddlersRage() {
        // Enchant creature
        target(TargetFilters.creature());
        // Enchanted creature gets +2/+1 and has tantrum.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 1, Set.of(Keyword.TANTRUM), GrantScope.ENCHANTED_CREATURE));
    }
}
