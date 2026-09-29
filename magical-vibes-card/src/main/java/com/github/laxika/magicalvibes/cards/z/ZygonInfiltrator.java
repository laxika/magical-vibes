package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureWhileTargetTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "63")
public class ZygonInfiltrator extends Card {

    public ZygonInfiltrator() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN),
                        new BecomeCopyOfTargetCreatureWhileTargetTappedEffect()
                ),
                "Body-print — {2}{U}: Tap another target creature and put a stun counter on it. "
                        + "This creature becomes a copy of that creature for as long as that creature remains tapped. "
                        + "Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
