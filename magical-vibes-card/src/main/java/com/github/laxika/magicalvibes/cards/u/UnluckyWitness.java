package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect;

@CardRegistration(set = "SNC", collectorNumber = "128")
public class UnluckyWitness extends Card {

    public UnluckyWitness() {
        addEffect(EffectSlot.ON_DEATH, new ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(2));
    }
}
