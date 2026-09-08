package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "10")
public class DaruStinger extends Card {

    public DaruStinger() {
        // Amplify 1 — This creature enters with a +1/+1 counter on it for each Soldier card
        // revealed from your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new MatchingCardsInHand(CountScope.CONTROLLER,
                        new CardSubtypePredicate(CardSubtype.SOLDIER))));

        // {T}: This creature deals damage equal to the number of +1/+1 counters on it to target
        // attacking or blocking creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                "{T}: This creature deals damage equal to the number of +1/+1 counters on it to target attacking or blocking creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentIsBlockingPredicate()
                                ))
                        )),
                        "Target must be an attacking or blocking creature"
                )
        ));
    }
}
