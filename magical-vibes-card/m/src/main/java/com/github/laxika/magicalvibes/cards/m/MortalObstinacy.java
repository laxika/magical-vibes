package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "JOU", collectorNumber = "17")
public class MortalObstinacy extends Card {

    public MortalObstinacy() {
        // Enchant creature you control. Enchanted creature gets +1/+1.
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));

        // Whenever enchanted creature deals combat damage to a player, you may sacrifice this Aura.
        // If you do, destroy target enchantment.
        target(TargetFilters.enchantment())
                .addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                        SequenceEffect.of(new SacrificeSelfEffect(),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new DestroyTargetPermanentEffect(
                                new PermanentIsEnchantmentPredicate()))),
                        "Sacrifice Mortal Obstinacy to destroy target enchantment?"));
    }
}
