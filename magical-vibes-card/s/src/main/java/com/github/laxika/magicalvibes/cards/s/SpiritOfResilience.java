package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterThenMayBecomeCopyOfCardsEffect;

@CardRegistration(set = "SOC", collectorNumber = "37")
@CardRegistration(set = "SOC", collectorNumber = "85")
public class SpiritOfResilience extends Card {

    public SpiritOfResilience() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new PutCounterThenMayBecomeCopyOfCardsEffect());
    }
}
