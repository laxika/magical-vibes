package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "27")
@CardRegistration(set = "OPCA", collectorNumber = "57")
public class NornsDominion extends Card {

    public NornsDominion() {
        PermanentAllOfPredicate unmarkedNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.FATE))
        ));

        addTrigger(Triggers.planeswalkAwayFromThis(), SequenceEffect.of(
                new DestroyAllPermanentsEffect(unmarkedNonland, false, EachPermanentScope.ALL_PLAYERS, null, null),
                RemoveCountersEffect.allFrom(Subjects.allPermanents(), CounterType.FATE)));

        target(TargetFilters.permanent()).addTrigger(Triggers.chaosEnsues(),
                new MayEffect(new PutCountersEffect(CounterType.FATE),
                        "Put a fate counter on target permanent?"));
    }
}
