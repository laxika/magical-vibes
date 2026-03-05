package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "172")
public class LivewireLash extends Card {

    public LivewireLash() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(2, 0));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new DealDamageToAnyTargetEffect(2));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
