package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "EMN", collectorNumber = "107")
public class SuccumbToTemptation extends Card {

    public SuccumbToTemptation() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2));
    }
}
