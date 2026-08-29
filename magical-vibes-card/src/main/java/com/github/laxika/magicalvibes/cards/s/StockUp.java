package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "DFT", collectorNumber = "67")
public class StockUp extends Card {

    public StockUp() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(5), new Fixed(2), null,
                LookDestination.BOTTOM_OF_LIBRARY, false));
    }
}
