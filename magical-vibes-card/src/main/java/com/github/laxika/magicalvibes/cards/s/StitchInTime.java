package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;

@CardRegistration(set = "GPT", collectorNumber = "132")
public class StitchInTime extends Card {

    public StitchInTime() {
        addEffect(EffectSlot.SPELL, new FlipCoinWinEffect(new ControllerExtraTurnEffect(1)));
    }
}
