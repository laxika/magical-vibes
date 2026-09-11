package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;

@CardRegistration(set = "HOB", collectorNumber = "105")
public class MistyMountainsRaider extends Card {

    public MistyMountainsRaider() {
        addEffect(EffectSlot.ON_ATTACK, new AmassGoblinsEffect(2));
    }
}
