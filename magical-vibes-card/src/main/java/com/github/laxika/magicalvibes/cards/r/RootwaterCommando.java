package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IslandwalkEffect;

public class RootwaterCommando extends Card {

    public RootwaterCommando() {
        addEffect(EffectSlot.STATIC, new IslandwalkEffect());
    }
}
