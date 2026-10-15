package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "LGN", collectorNumber = "49")
public class PrimocEscapee extends Card {

    public PrimocEscapee() {
        addCycling("{2}");
    }
}
