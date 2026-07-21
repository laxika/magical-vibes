package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsPowerToAttackedTargetEffect;

@CardRegistration(set = "ARB", collectorNumber = "57")
public class MageSlayer extends Card {

    public MageSlayer() {
        // Whenever equipped creature attacks, it deals damage equal to its power to the player or
        // planeswalker it's attacking.
        addEffect(EffectSlot.ON_ATTACK, new EquippedCreatureDealsPowerToAttackedTargetEffect());

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
