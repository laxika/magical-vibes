package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "ALA", collectorNumber = "31")
public class YokedPlowbeast extends Card {

    public YokedPlowbeast() {
        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
