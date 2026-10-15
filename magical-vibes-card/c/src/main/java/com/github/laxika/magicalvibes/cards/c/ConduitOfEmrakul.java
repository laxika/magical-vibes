package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaAtNextMainPhaseEffect;

/**
 * Conduit of Emrakul — back face of Conduit of Storms.
 * Whenever this creature attacks, add {C}{C} at the beginning of your next main phase this turn.
 */
public class ConduitOfEmrakul extends Card {

    public ConduitOfEmrakul() {
        // Whenever this creature attacks, add {C}{C} at the beginning of your next main phase this turn.
        addEffect(EffectSlot.ON_ATTACK,
                new RegisterDelayedManaAtNextMainPhaseEffect(ManaColor.COLORLESS, 2));
    }
}
