package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "11")
public class DaringMechanic extends Card {

    public DaringMechanic() {
        PermanentHasAnySubtypePredicate mountOrVehicle =
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MOUNT, CardSubtype.VEHICLE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "{3}{W}: Put a +1/+1 counter on target Mount or Vehicle.",
                new PermanentPredicateTargetFilter(mountOrVehicle, "Target must be a Mount or Vehicle")
        ));
    }
}
