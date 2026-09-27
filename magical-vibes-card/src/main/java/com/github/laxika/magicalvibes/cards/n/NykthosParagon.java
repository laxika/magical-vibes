package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NykthosParagonLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "MH2", collectorNumber = "22")
public class NykthosParagon extends Card {

    public NykthosParagon() {
        // Whenever you gain life, you may put that many +1/+1 counters on each creature you control.
        // Do this only once each turn.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                OncePerTurnTriggerEffect.markOnAcceptance(new NykthosParagonLifeGainEffect()));
    }
}
