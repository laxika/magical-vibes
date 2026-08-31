package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class ThatsMine extends Card {

    public ThatsMine() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
    }
}
