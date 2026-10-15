package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "76")
public class SpiritAway extends Card {

    public SpiritAway() {
        // Enchant creature
        target(TargetFilters.creature())
                // You control enchanted creature.
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect())
                // Enchanted creature gets +2/+2 and has flying.
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(2, 2, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));
    }
}
