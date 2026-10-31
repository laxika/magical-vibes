package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

public class ProfaneInsight extends Card {

    public ProfaneInsight() {
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new LoseLifeEffect(1));
    }
}
