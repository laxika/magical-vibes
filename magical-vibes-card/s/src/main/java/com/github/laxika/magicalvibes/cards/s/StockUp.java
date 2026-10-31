package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "DFT", collectorNumber = "67")
@CardRegistration(set = "SOA", collectorNumber = "24")
public class StockUp extends Card {

    public StockUp() {
        addSpellEffect(new LookAtTopCardsEffect(
                new Fixed(5), new Fixed(2), null,
                LookDestination.BOTTOM_OF_LIBRARY, false));
    }
}
