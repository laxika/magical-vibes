package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Unholy Fiend — back face of Cloistered Youth.
 * 3/3 Horror.
 * At the beginning of your end step, you lose 1 life.
 */
public class UnholyFiend extends Card {

    public UnholyFiend() {
        // At the beginning of your end step, you lose 1 life.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new LoseLifeEffect(1));
    }
}
