package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SailIntoTheWestEffect;

@CardRegistration(set = "LTC", collectorNumber = "68")
@CardRegistration(set = "LTC", collectorNumber = "149")
@CardRegistration(set = "LTC", collectorNumber = "475")
public class SailIntoTheWest extends Card {

    public SailIntoTheWest() {
        addSpellEffect(new SailIntoTheWestEffect());
    }
}
