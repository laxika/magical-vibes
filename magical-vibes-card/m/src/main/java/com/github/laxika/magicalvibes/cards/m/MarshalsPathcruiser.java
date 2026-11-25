package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "236")
public class MarshalsPathcruiser extends Card {

    public MarshalsPathcruiser() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(
                        new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null,
                                Set.of(CardType.CREATURE), Subjects.source(), Duration.PERMANENT, null,
                                Set.of(), false, false),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "Exhaust — {W}{U}{B}{R}{G}: This Vehicle becomes an artifact creature. Put two +1/+1 counters on it."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(5), AnimatePermanentsEffect.crew()),
                "Crew 5"
        ));
    }
}
