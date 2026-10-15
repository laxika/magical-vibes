package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect;

@CardRegistration(set = "USG", collectorNumber = "173")
public class Antagonism extends Card {

    public Antagonism() {
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect(2));
    }
}
