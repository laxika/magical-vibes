package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "107")
public class FlayerHusk extends Card {

    public FlayerHusk() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +1/+1
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(1, 1));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
