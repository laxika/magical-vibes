package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

/**
 * Unholy Fiend — back face of Cloistered Youth.
 * 3/3 Horror.
 * At the beginning of your end step, you lose 1 life.
 */
public class UnholyFiend extends Card {

    public UnholyFiend() {
        // At the beginning of your end step, you lose 1 life.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new LoseLifeEffect(1));
    }
}
