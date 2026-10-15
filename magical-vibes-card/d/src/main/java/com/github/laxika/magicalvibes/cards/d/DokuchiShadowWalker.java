package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "NEO", collectorNumber = "94")
public class DokuchiShadowWalker extends Card {

    public DokuchiShadowWalker() {
        addNinjutsu("{3}{B}");
    }
}
