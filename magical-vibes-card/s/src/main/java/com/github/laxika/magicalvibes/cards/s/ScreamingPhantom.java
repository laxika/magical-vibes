package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "LCI", collectorNumber = "118")
public class ScreamingPhantom extends Card {

    public ScreamingPhantom() {
        // Whenever this creature attacks, mill a card.
        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.CONTROLLER));
    }
}
