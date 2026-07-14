package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "182")
@CardRegistration(set = "6ED", collectorNumber = "175")
public class FinalFortune extends Card {

    public FinalFortune() {
        // "Take an extra turn after this one."
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        // "At the beginning of that turn's end step, you lose the game."
        addEffect(EffectSlot.SPELL, new RegisterLoseGameAtEndStepEffect());
    }
}
