package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "32")
public class VedalkenGhoul extends Card {

    public VedalkenGhoul() {
        // Whenever this creature becomes blocked, defending player loses 4 life.
        addTrigger(Triggers.becomesBlocked(),
                new LoseLifeEffect(4, LoseLifeRecipient.DEFENDING_PLAYER));
    }
}
