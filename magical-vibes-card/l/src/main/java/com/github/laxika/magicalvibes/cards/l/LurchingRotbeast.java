package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "HOU", collectorNumber = "69")
public class LurchingRotbeast extends Card {

    public LurchingRotbeast() {
        // Cycling {B} ({B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{B}");
    }
}
