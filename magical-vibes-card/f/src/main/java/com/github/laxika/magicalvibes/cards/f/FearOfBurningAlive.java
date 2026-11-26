package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "135")
public class FearOfBurningAlive extends Card {

    public FearOfBurningAlive() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.to(Subjects.eachOpponent(), 4));
        addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                DealDamageEffect.toTargetCreature(new EventValue(), new PermanentControlledByTriggeringPlayerPredicate()));
    }
}
