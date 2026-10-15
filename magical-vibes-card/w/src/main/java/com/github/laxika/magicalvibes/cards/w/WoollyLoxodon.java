package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "158")
@CardRegistration(set = "A25", collectorNumber = "195")
public class WoollyLoxodon extends Card {

    public WoollyLoxodon() {
        addMorph("{5}{G}");
    }
}
