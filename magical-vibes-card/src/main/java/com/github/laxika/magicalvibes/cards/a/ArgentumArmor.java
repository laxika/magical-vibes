package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

@CardRegistration(set = "SOM", collectorNumber = "137")
public class ArgentumArmor extends Card {

    public ArgentumArmor() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(6, 6));
        addEffect(EffectSlot.ON_ATTACK, new DestroyTargetPermanentEffect(false));
        addActivatedAbility(new EquipActivatedAbility("{6}"));
    }
}
