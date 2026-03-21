package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageFromEachSourceToAttachedCreatureEffect;

@CardRegistration(set = "DOM", collectorNumber = "228")
public class ShieldOfTheRealm extends Card {

    public ShieldOfTheRealm() {
        addEffect(EffectSlot.STATIC, new PreventXDamageFromEachSourceToAttachedCreatureEffect(2));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
