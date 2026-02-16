package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class Sift extends Card {

    public Sift() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new DiscardCardEffect());
    }
}
