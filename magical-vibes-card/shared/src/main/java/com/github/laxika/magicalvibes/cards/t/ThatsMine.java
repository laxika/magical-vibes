package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class ThatsMine extends Card {

    public ThatsMine() {
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
    }
}
