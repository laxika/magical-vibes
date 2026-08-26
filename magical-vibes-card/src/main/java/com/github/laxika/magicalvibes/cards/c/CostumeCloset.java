package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "5")
public class CostumeCloset extends Card {

    public CostumeCloset() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MoveCounterFromSourceToTargetCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Move a +1/+1 counter from this artifact onto target creature you control. Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsModifiedPredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
