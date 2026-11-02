package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMD", collectorNumber = "59")
@CardRegistration(set = "CM2", collectorNumber = "48")
public class Riddlekeeper extends Card {

    public Riddlekeeper() {
        // Whenever a creature attacks you or a planeswalker you control, that creature's controller
        // mills two cards.
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                new MillEffect(2, MillRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
