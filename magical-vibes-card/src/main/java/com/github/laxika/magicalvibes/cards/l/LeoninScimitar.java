package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "331")
public class LeoninScimitar extends Card {

    public LeoninScimitar() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(1, 1));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
