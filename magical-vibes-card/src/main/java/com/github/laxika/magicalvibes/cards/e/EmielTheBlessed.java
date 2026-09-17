package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "10")
public class EmielTheBlessed extends Card {

    public EmielTheBlessed() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(FlickerEffect.flickerTarget()),
                "{3}: Exile another target creature you control, then return it to the battlefield under its owner's control.",
                new ControlledPermanentPredicateTargetFilter(
                        anotherCreature,
                        "Target must be another creature you control"
                )
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{G/W}",
                SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new ConditionalEffect(
                                new TargetPermanentMatches(
                                        new PermanentHasSubtypePredicate(CardSubtype.UNICORN)),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                ),
                "Pay {G/W} to put a +1/+1 counter on the entering creature?"
        ));
    }
}
