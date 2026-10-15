package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "227")
public class InquisitorsFlail extends Card {

    public InquisitorsFlail() {
        addEffect(EffectSlot.STATIC, new DoubleEquippedCreatureCombatDamageEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
