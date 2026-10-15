package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "52")
public class Sandskin extends Card {

    public Sandskin() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToAndByEnchantedCreatureEffect());
    }
}
