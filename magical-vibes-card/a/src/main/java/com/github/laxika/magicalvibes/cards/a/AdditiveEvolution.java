package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "139")
public class AdditiveEvolution extends Card {

    public AdditiveEvolution() {
        // When this enchantment enters, create a 0/0 green and blue Fractal creature token.
        // Put three +1/+1 counters on it.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(
                "Fractal", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL), 0),
                new com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new com.github.laxika.magicalvibes.model.amount.Fixed(3)));

        // At the beginning of combat on your turn, put a +1/+1 counter on target creature
        // you control. It gains vigilance until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a creature you control"
        ))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET));
    }
}
