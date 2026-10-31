package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

public class TillAndTend extends Card {

    public TillAndTend() {
        addSpellEffect(new PlayAdditionalLandsEffect(1));
    }
}
