package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenBoostSourceEffect;

@CardRegistration(set = "CMD", collectorNumber = "129")
public class ManaChargedDragon extends Card {

    public ManaChargedDragon() {
        addEffect(EffectSlot.ON_ATTACK, new EachPlayerPaysAnyManaThenBoostSourceEffect());
        addEffect(EffectSlot.ON_BLOCK, new EachPlayerPaysAnyManaThenBoostSourceEffect());
    }
}
