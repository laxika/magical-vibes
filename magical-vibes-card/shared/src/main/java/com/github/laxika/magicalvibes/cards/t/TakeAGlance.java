package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

public class TakeAGlance extends Card {

    public TakeAGlance() {
        addSpellEffect(new ScryEffect(2));
    }
}
