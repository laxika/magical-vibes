package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "209")
public class SylvokLifestaff extends Card {

    public SylvokLifestaff() {
        // Equipped creature gets +1/+0
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(1, 0));

        // Whenever equipped creature dies, you gain 3 life
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new GainLifeEffect(3));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
