package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;

@CardRegistration(set = "SOM", collectorNumber = "139")
public class BarbedBattlegear extends Card {

    public BarbedBattlegear() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(4, -1));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
