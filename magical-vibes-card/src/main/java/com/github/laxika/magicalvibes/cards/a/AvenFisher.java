package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

public class AvenFisher extends Card {

    public AvenFisher() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
