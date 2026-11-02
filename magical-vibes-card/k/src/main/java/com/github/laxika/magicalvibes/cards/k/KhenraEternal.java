package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "66")
@CardRegistration(set = "AKR", collectorNumber = "109")
public class KhenraEternal extends Card {

    public KhenraEternal() {
        // Afflict 1 — whenever this creature becomes blocked, the defending player loses 1 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addTrigger(Triggers.becomesBlocked(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
