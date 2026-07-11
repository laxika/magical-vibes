package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "136")
public class LastChance extends Card {

    public LastChance() {
        // "Take an extra turn after this one."
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        // "At the beginning of that turn's end step, you lose the game."
        addEffect(EffectSlot.SPELL, new RegisterLoseGameAtEndStepEffect());
    }
}
