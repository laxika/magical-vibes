package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;

public class Peek extends Card {

    public Peek() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new LookAtHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
