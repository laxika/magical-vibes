package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class BatheInGold extends Card {

    public BatheInGold() {
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
    }
}
