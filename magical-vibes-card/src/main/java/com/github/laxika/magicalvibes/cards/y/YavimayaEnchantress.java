package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEnchantmentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "310")
public class YavimayaEnchantress extends Card {

    public YavimayaEnchantress() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerEnchantmentOnBattlefieldEffect(1, 1));
    }
}
