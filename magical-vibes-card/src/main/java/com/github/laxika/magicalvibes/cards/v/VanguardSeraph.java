package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FDN", collectorNumber = "28")
public class VanguardSeraph extends Card {

    public VanguardSeraph() {
        // Whenever you gain life for the first time each turn, surveil 1.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new OncePerTurnTriggerEffect(new SurveilEffect(1)));
    }
}
