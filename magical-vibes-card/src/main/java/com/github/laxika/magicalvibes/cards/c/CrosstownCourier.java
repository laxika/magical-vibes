package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "RTR", collectorNumber = "34")
public class CrosstownCourier extends Card {

    public CrosstownCourier() {
        // The damaged player mills cards equal to the combat damage dealt (bound as the event value).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillEffect(new EventValue(), MillRecipient.TARGET_PLAYER));
    }
}
