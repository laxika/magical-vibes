package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ECL", collectorNumber = "6")
public class BarkOfDoran extends Card {

    public BarkOfDoran() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(0, 1));
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect());
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
