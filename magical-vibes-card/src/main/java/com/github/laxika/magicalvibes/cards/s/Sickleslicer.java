package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;

@CardRegistration(set = "NPH", collectorNumber = "157")
public class Sickleslicer extends Card {

    public Sickleslicer() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
