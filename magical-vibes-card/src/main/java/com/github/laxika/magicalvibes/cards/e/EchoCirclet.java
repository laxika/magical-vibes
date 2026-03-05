package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "153")
public class EchoCirclet extends Card {

    public EchoCirclet() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
