package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;

@CardRegistration(set = "ONC", collectorNumber = "26")
@CardRegistration(set = "ONC", collectorNumber = "36")
public class TangleweaveArmor extends Card {

    public TangleweaveArmor() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +X/+X, where X is the greatest mana value among your commanders
        GreatestManaValueAmongOwnedCommanders greatestCommanderManaValue =
                new GreatestManaValueAmongOwnedCommanders();
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                greatestCommanderManaValue,
                greatestCommanderManaValue,
                GrantScope.EQUIPPED_CREATURE));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
