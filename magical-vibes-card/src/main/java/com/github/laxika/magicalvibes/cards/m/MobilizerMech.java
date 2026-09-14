package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "65")
public class MobilizerMech extends Card {

    public MobilizerMech() {
        PermanentPredicate otherVehicle = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        target(new ControlledPermanentPredicateTargetFilter(
                otherVehicle,
                "Target must be another Vehicle you control"
        ), 0, 1).addEffect(EffectSlot.ON_SELF_BECOMES_CREWED,
                new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, null));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
