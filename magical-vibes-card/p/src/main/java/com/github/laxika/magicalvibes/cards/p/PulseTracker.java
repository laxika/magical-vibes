package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "62")
@CardRegistration(set = "DDM", collectorNumber = "43")
public class PulseTracker extends Card {

    public PulseTracker() {
        // Whenever this creature attacks, each opponent loses 1 life.
        addTrigger(Triggers.attacks(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
