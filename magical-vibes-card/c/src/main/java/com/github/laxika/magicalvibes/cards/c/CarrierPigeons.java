package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ALL", collectorNumber = "1a")
@CardRegistration(set = "ALL", collectorNumber = "1b")
public class CarrierPigeons extends Card {

    public CarrierPigeons() {
        // "When this creature enters, draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
