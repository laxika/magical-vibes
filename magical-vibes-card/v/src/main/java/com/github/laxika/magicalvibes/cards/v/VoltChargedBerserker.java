package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

public class VoltChargedBerserker extends Card {

    public VoltChargedBerserker() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
