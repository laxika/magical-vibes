package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;

public class CovetousGeist extends Card {

    public CovetousGeist() {
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
