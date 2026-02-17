package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;

public class HowlingMine extends Card {

    public HowlingMine() {
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1, true));
    }
}
