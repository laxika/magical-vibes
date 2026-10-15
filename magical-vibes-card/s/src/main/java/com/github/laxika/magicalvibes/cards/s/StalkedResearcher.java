package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

@CardRegistration(set = "DSK", collectorNumber = "73")
public class StalkedResearcher extends Card {

    public StalkedResearcher() {
        CanAttackAsThoughNoDefenderEffect canAttack = new CanAttackAsThoughNoDefenderEffect();
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, canAttack);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, canAttack);
    }
}
