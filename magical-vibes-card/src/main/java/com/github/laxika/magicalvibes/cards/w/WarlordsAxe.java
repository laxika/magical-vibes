package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "220")
public class WarlordsAxe extends Card {

    public WarlordsAxe() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(3, 1));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
