package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;

@CardRegistration(set = "RTR", collectorNumber = "1")
@CardRegistration(set = "SLD", collectorNumber = "1377")
@CardRegistration(set = "C15", collectorNumber = "58")
@CardRegistration(set = "C21", collectorNumber = "83")
@CardRegistration(set = "KHC", collectorNumber = "18")
@CardRegistration(set = "CM2", collectorNumber = "16")
public class AngelOfSerenity extends Card {

    public AngelOfSerenity() {
        // When this creature enters, you may exile up to three other target creatures from the
        // battlefield and/or creature cards from graveyards.
        // When this creature leaves the battlefield, return the exiled cards to their owners' hands.
        // Each exiled card is registered for the separate leaves-the-battlefield ability.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCreaturesUntilSourceLeavesEffect(3, true, false, true));
    }
}
