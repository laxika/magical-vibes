package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "128")
@CardRegistration(set = "MSC", collectorNumber = "812")
public class UnluckyWitness extends Card {

    public UnluckyWitness() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect.mayPlayOneOfThem(2));
    }
}
