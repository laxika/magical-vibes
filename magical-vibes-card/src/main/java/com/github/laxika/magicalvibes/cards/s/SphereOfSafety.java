package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "RTR", collectorNumber = "24")
public class SphereOfSafety extends Card {

    public SphereOfSafety() {
        // Creatures can't attack you or planeswalkers you control unless their controller
        // pays {X} for each of those creatures, where X is the number of enchantments you control.
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER)
        ));
    }
}
