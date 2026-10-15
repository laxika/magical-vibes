package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class GentlemansRise extends Card {

    public GentlemansRise() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(1));
    }
}
