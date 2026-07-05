package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "310")
public class YavimayaEnchantress extends Card {

    public YavimayaEnchantress() {
        // Yavimaya Enchantress gets +1/+1 for each enchantment on the battlefield.
        PermanentCount enchantmentsOnBattlefield =
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(enchantmentsOnBattlefield, enchantmentsOnBattlefield));
    }
}
