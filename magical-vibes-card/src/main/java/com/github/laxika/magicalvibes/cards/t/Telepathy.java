package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;

public class Telepathy extends Card {

    public Telepathy() {
        addEffect(EffectSlot.STATIC, new RevealOpponentHandsEffect());
    }
}
