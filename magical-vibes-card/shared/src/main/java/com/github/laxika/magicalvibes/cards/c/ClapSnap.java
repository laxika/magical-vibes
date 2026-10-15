package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;

public class ClapSnap extends Card {

    public ClapSnap() {
        addEffect(EffectSlot.SPELL, new AmassGoblinsEffect(2));
    }
}
