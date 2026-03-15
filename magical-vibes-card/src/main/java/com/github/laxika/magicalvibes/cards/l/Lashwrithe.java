package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;

@CardRegistration(set = "NPH", collectorNumber = "143")
public class Lashwrithe extends Card {

    public Lashwrithe() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +1/+1 for each Swamp you control
        addEffect(EffectSlot.STATIC, new BoostCreaturePerControlledSubtypeEffect(
                CardSubtype.SWAMP, 1, 1, GrantScope.EQUIPPED_CREATURE));

        // Equip {B/P}{B/P}
        addActivatedAbility(new EquipActivatedAbility("{B/P}{B/P}"));
    }
}
