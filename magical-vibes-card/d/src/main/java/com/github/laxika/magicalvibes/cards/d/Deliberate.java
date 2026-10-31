package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "ZNR", collectorNumber = "56")
public class Deliberate extends Card {

    public Deliberate() {
        addSpellEffect(new ScryEffect(2));
        addSpellEffect(new DrawCardEffect(1));
    }
}
