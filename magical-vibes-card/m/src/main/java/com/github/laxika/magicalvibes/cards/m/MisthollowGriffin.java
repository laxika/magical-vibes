package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileCast;

@CardRegistration(set = "AVR", collectorNumber = "68")
public class MisthollowGriffin extends Card {

    public MisthollowGriffin() {
        // You may cast this card from exile.
        addCastingOption(new ExileCast());
    }
}
