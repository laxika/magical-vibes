package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "136")
@CardRegistration(set = "DDK", collectorNumber = "56")
public class GangOfDevils extends Card {

    public GangOfDevils() {
        // When this creature dies, it deals 3 damage divided as you choose among one, two, or three targets.
        addTrigger(Triggers.dies(ObjectRelation.THIS), DealDividedDamageEffect.chosenAmongAnyTargetsEtb(3, 3));
    }
}
