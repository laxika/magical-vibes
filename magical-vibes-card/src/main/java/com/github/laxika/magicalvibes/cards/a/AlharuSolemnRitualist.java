package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "8")
public class AlharuSolemnRitualist extends Card {

    public AlharuSolemnRitualist() {
        PermanentPredicateTargetFilter otherCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature");

        // When Alharu enters, put a +1/+1 counter on each of up to two other target creatures.
        target(otherCreature, 0, 2).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // Whenever a nontoken creature you control with a +1/+1 counter on it dies, create a 1/1
        // white Spirit creature token with flying.
        CreateTokenEffect spiritToken = CreateTokenEffect.whiteSpirit(1);
        TriggeringPermanentConditionalEffect counteredCreatureDies =
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE), spiritToken);
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, counteredCreatureDies);
        addEffect(EffectSlot.ON_DEATH, counteredCreatureDies);
    }
}
