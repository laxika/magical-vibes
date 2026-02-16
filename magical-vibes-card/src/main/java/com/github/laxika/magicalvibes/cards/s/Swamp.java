package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

public class Swamp extends Card {

    public Swamp() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));
    }
}
