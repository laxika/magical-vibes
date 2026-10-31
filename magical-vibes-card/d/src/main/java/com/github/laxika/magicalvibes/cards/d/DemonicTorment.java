package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "95")
@CardRegistration(set = "ME3", collectorNumber = "62")
public class DemonicTorment extends Card {

    public DemonicTorment() {
        target(TargetFilters.creature())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect(true, false))
                .addStaticEffect(new PreventAllDamageDealtByEnchantedCreatureEffect(true));
    }
}
