package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

public class TillAndTend extends Card {

    public TillAndTend() {
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
    }
}
