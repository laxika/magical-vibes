package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "136")
@CardRegistration(set = "DBL", collectorNumber = "136")
public class FalkenrathPerforator extends Card {

    public FalkenrathPerforator() {
        addTrigger(Triggers.attacks(), DealDamageEffect.to(Subjects.defendingPlayer(), 1));
    }
}
