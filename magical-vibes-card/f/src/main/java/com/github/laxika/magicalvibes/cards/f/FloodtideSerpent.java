package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "BNG", collectorNumber = "41")
public class FloodtideSerpent extends Card {

    public FloodtideSerpent() {
        PermanentIsEnchantmentPredicate enchantment = new PermanentIsEnchantmentPredicate();

        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsPermanentCount(1, enchantment),
                "you return an enchantment you control to its owner's hand"));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessReturnToHandEffect(
                1, enchantment, "an enchantment you control"));
    }
}
