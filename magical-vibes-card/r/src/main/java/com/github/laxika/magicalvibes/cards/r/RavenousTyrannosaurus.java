package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureWithExcessToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "93")
@CardRegistration(set = "REX", collectorNumber = "18")
@CardRegistration(set = "REX", collectorNumber = "43")
public class RavenousTyrannosaurus extends Card {

    public RavenousTyrannosaurus() {
        addStaticEffect(new DevourEffect(3));

        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        target(new PermanentPredicateTargetFilter(otherCreature, "Target must be another creature"), 0, 1)
                .addTrigger(Triggers.attacks(),
                        new DealDamageToTargetCreatureWithExcessToControllerEffect(new SourcePower(), false));
    }
}
