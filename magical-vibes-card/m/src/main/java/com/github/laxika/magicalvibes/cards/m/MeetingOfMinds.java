package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MOM", collectorNumber = "66")
public class MeetingOfMinds extends Card {

    public MeetingOfMinds() {
        // Convoke is granted by the Scryfall-loaded keyword and handled by the casting service.
        // Draw two cards.
        addSpellEffect(new DrawCardEffect(2));
    }
}
