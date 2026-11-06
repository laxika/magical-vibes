package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "108")
public class SatyrFiredancer extends Card {

    public SatyrFiredancer() {
        addTrigger(Triggers.instantOrSorceryYouControlDealsDamageToOpponent(),
                new DealDamageToTargetCreatureEffect(new EventValue(), false,
                        new PermanentControlledByTriggeringPlayerPredicate()));
    }
}
