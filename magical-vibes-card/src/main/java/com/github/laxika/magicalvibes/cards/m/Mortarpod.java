package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "115")
public class Mortarpod extends Card {

    public Mortarpod() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +0/+1
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(0, 1));

        // Equipped creature has "Sacrifice this creature: This creature deals 1 damage to any target."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        null,
                        List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                        "Sacrifice this creature: This creature deals 1 damage to any target."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
