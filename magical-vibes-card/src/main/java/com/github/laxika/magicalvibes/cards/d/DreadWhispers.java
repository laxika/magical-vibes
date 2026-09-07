package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

public class DreadWhispers extends Card {

    public DreadWhispers() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(1));
    }
}
