package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class StokeGenius extends Card {

    public StokeGenius() {
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new DrawCardEffect(2));
    }
}
