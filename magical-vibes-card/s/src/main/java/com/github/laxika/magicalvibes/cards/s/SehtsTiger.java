package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceToControllerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "31")
@CardRegistration(set = "C17", collectorNumber = "72")
public class SehtsTiger extends Card {

    public SehtsTiger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantProtectionChoiceToControllerUntilEndOfTurnEffect());
    }
}
