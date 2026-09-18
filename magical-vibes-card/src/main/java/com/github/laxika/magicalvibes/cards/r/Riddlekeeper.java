package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "CMD", collectorNumber = "59")
public class Riddlekeeper extends Card {

    public Riddlekeeper() {
        // Whenever a creature attacks you or a planeswalker you control, that creature's controller
        // mills two cards.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new MillEffect(2, MillRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
