package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

@CardRegistration(set = "VOW", collectorNumber = "80")
public class SteelcladSpirit extends Card {

    public SteelcladSpirit() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new CanAttackAsThoughNoDefenderEffect());
    }
}
