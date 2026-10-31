package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

public class DreadWhispers extends Card {

    public DreadWhispers() {
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new LoseLifeEffect(1));
    }
}
