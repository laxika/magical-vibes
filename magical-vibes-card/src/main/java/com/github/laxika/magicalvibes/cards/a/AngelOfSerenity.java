package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;

@CardRegistration(set = "RTR", collectorNumber = "1")
public class AngelOfSerenity extends Card {

    public AngelOfSerenity() {
        // When this creature enters, you may exile up to three other target creatures from the
        // battlefield and/or creature cards from graveyards.
        // When this creature leaves the battlefield, return the exiled cards to their owners' hands.
        // The leave trigger is implicit: each exiled card is registered as a pending return keyed
        // on this permanent, and "up to three" carries the "you may" (zero targets is a legal pick).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCreaturesUntilSourceLeavesEffect(3, true));
    }
}
