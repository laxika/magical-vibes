package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "17")
public class MortalObstinacy extends Card {

    public MortalObstinacy() {
        // Enchant creature you control. Enchanted creature gets +1/+1.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));

        // Whenever enchanted creature deals combat damage to a player, you may sacrifice this Aura.
        // If you do, destroy target enchantment.
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                        new SacrificeSelfThenEffect(new DestroyTargetPermanentEffect(
                                new PermanentIsEnchantmentPredicate())),
                        "Sacrifice Mortal Obstinacy to destroy target enchantment?"));
    }
}
