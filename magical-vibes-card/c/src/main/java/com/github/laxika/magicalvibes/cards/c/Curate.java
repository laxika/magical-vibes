package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "BRO", collectorNumber = "44")
@CardRegistration(set = "STX", collectorNumber = "40")
@CardRegistration(set = "MKC", collectorNumber = "99")
public class Curate extends Card {

    public Curate() {
        // Surveil 2, then draw a card.
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(new DrawCardEffect(1));
    }
}
