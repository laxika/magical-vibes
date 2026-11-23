package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Conduit of Emrakul — back face of Conduit of Storms.
 * Whenever this creature attacks, add {C}{C} at the beginning of your next main phase this turn.
 */
public class ConduitOfEmrakul extends Card {

    public ConduitOfEmrakul() {
        // Whenever this creature attacks, add {C}{C} at the beginning of your next main phase this turn.
        addTrigger(Triggers.attacks(),
                RegisterDelayedStepTriggerEffect.atNextMainPhaseThisTurn(ActivePlayer.YOU,
                        new AwardManaEffect(ManaColor.COLORLESS, 2)));
    }
}
