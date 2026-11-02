package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "95")
@CardRegistration(set = "WHO", collectorNumber = "700")
public class SibyllineSoothsayer extends Card {

    public SibyllineSoothsayer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect(3, 3));
    }
}
