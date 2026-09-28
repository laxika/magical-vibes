package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetPermanentCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "45")
@CardRegistration(set = "WHO", collectorNumber = "360")
public class TheFloodOfMars extends Card {

    public TheFloodOfMars() {
        PermanentPredicate anotherCreatureOrLand = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()))));

        target(new PermanentPredicateTargetFilter(
                anotherCreatureOrLand,
                "Target must be another creature or land"))
                .addEffect(EffectSlot.ON_ATTACK, new PutCounterOnTargetPermanentEffect(CounterType.FLOOD))
                .addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsLandPredicate()),
                        new GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype.ISLAND, CounterType.FLOOD),
                        false))
                .addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsCreaturePredicate()),
                        new BecomeTargetPermanentCopyOfSourceEffect(),
                        false));
    }
}
