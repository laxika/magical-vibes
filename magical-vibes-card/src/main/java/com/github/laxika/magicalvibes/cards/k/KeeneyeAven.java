package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "LGN", collectorNumber = "41")
public class KeeneyeAven extends Card {

    public KeeneyeAven() {
        addCycling("{2}");
    }
}
