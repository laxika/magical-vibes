package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class StokeGenius extends Card {

    public StokeGenius() {
        addEffect(EffectSlot.SPELL, new DiscardHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
