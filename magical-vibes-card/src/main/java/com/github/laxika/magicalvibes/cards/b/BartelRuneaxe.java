package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;

@CardRegistration(set = "LEG", collectorNumber = "222")
@CardRegistration(set = "ME3", collectorNumber = "145")
public class BartelRuneaxe extends Card {

    public BartelRuneaxe() {
        addEffect(EffectSlot.STATIC, new CantBeEnchantedByOtherAurasEffect());
    }
}
