package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaAtNextMainPhaseEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Conduit of Emrakul — back face of Conduit of Storms.
 * Whenever this creature attacks, add {C}{C} at the beginning of your next main phase this turn.
 */
public class ConduitOfEmrakul extends Card {

    public ConduitOfEmrakul() {
        // Whenever this creature attacks, add {C}{C} at the beginning of your next main phase this turn.
        addTrigger(Triggers.attacks(),
                new RegisterDelayedManaAtNextMainPhaseEffect(ManaColor.COLORLESS, 2));
    }
}
