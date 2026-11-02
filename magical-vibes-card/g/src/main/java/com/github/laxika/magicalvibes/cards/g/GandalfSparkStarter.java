package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "97")
public class GandalfSparkStarter extends Card {

    public GandalfSparkStarter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDividedDamageEffect.chosenAmongAnyTargetsEtb(3, 3));
    }
}
