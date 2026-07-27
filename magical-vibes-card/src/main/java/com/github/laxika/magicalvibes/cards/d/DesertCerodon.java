package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "128")
public class DesertCerodon extends Card {

    public DesertCerodon() {
        // Cycling {R} ({R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{R}");
    }
}
