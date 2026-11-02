package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "46")
@CardRegistration(set = "AKR", collectorNumber = "80")
public class SpellweaverEternal extends Card {

    public SpellweaverEternal() {
        // Afflict 2 — whenever this creature becomes blocked, the defending player loses 2 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addTrigger(Triggers.becomesBlocked(), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
    }
}
