package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "167")
@CardRegistration(set = "USG", collectorNumber = "161")
public class TaintedAether extends Card {

    public TaintedAether() {
        // Whenever a creature enters, its controller sacrifices a creature or land of their choice.
        addTrigger(Triggers.enters(new PermanentIsCreaturePredicate(), ObjectRelation.ANY),
                new SacrificePermanentsEffect(1,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate())),
                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
