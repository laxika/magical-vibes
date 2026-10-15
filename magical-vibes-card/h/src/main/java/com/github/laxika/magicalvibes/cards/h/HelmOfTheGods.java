package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "ORI", collectorNumber = "230")
public class HelmOfTheGods extends Card {

    public HelmOfTheGods() {
        // Equipped creature gets +1/+1 for each enchantment you control.
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER),
                new PermanentCount(new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER),
                GrantScope.EQUIPPED_CREATURE));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
