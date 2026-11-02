package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "34")
@CardRegistration(set = "AKR", collectorNumber = "61")
public class EternalOfHarshTruths extends Card {

    public EternalOfHarshTruths() {
        // Afflict 2 — whenever this creature becomes blocked, the defending player loses 2 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addTrigger(Triggers.becomesBlocked(), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
        // Whenever this creature attacks and isn't blocked, draw a card.
        addTrigger(Triggers.attacksUnblocked(), new DrawCardEffect());
    }
}
