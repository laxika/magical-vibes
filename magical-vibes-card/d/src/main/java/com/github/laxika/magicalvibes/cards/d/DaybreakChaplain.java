package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M19", collectorNumber = "10")
@CardRegistration(set = "M20", collectorNumber = "12")
public class DaybreakChaplain extends Card {

    public DaybreakChaplain() {
        // Vanilla creature; Lifelink is auto-loaded from Scryfall keywords.
    }
}
