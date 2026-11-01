package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "26")
@CardRegistration(set = "MKM", collectorNumber = "339")
public class NeighborhoodGuardian extends Card {

    public NeighborhoodGuardian() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                        new TriggeringPermanentConditionalEffect(
                                new PermanentPowerAtMostPredicate(2),
                                new BoostTargetCreatureEffect(1, 1)));
    }
}
