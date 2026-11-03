package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "190")
@CardRegistration(set = "BLC", collectorNumber = "232")
public class PrimevalBounty extends Card {

    public PrimevalBounty() {
        // Whenever you cast a creature spell, create a 3/3 green Beast creature token.
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()));

        // Whenever you cast a noncreature spell, put three +1/+1 counters on target creature you control.
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentControlledBySourceControllerPredicate())), "Target must be a creature you control")).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));

        // Landfall — Whenever a land you control enters, you gain 3 life.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new GainLifeEffect(3));
    }
}
