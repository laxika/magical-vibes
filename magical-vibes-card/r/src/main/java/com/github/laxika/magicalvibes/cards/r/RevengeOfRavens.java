package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "104")
@CardRegistration(set = "LTC", collectorNumber = "207")
public class RevengeOfRavens extends Card {

    public RevengeOfRavens() {
        // Whenever a creature attacks you or a planeswalker you control, that creature's controller
        // loses 1 life and you gain 1 life. The attacking creature is the trigger's non-targeting
        // targetId, so TARGET_PERMANENT_CONTROLLER routes the life loss to its controller.
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(), new GainLifeEffect(1));
    }
}
