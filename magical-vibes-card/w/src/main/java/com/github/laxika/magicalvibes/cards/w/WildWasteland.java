package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;

@CardRegistration(set = "PIP", collectorNumber = "71")
@CardRegistration(set = "PIP", collectorNumber = "396")
@CardRegistration(set = "PIP", collectorNumber = "599")
@CardRegistration(set = "PIP", collectorNumber = "924")
public class WildWasteland extends Card {

    public WildWasteland() {
        // Skip your draw step.
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());
        // At the beginning of your upkeep, exile the top two cards of your library. You may play
        // those cards this turn.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardMayPlayThisTurnEffect(2, false));
    }
}
