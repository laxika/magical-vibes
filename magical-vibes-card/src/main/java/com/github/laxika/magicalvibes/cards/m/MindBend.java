package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

public class MindBend extends Card {

    public MindBend() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect());
    }
}
