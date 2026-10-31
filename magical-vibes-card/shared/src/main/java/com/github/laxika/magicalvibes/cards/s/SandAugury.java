package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

public class SandAugury extends Card {

    public SandAugury() {
        addSpellEffect(new ScryEffect(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
