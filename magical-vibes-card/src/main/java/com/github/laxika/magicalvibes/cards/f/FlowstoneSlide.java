package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;

public class FlowstoneSlide extends Card {

    public FlowstoneSlide() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesXEffect(1, -1));
    }
}
