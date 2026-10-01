package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantBeTargetedByOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "147")
@CardRegistration(set = "UMA", collectorNumber = "222")
@CardRegistration(set = "AFC", collectorNumber = "192")
public class ShieldingPlax extends Card {

    public ShieldingPlax() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1))
                .addEffect(EffectSlot.STATIC,
                        new EnchantedPermanentCantBeTargetedByOpponentsEffect());
    }
}
