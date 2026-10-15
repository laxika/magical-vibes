package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

public class SandAugury extends Card {

    public SandAugury() {
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
