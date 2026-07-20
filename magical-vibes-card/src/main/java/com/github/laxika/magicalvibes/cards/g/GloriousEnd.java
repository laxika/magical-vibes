package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "AKH", collectorNumber = "133")
public class GloriousEnd extends Card {

    public GloriousEnd() {
        // "End the turn."
        addEffect(EffectSlot.SPELL, new EndTurnEffect());
        // "At the beginning of your next end step, you lose the game." Fires at the controller's own
        // next end step, skipping the (ended) current turn's end step and the intervening opponent turn.
        addEffect(EffectSlot.SPELL, new RegisterLoseGameAtEndStepEffect());
    }
}
