package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

public class TakeAGlance extends Card {

    public TakeAGlance() {
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
