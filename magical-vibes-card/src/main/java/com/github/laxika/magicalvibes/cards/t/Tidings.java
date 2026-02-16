package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class Tidings extends Card {

    public Tidings() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
    }
}
