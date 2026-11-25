package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "37")
@CardRegistration(set = "OTC", collectorNumber = "73")
public class BountyBoard extends Card {

    private static final PlayerSubject ITS_CONTROLLERS_OPPONENTS =
            Subjects.opponentsOf(Subjects.controllerOf(Subjects.triggering()));

    public BountyBoard() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersEffect(CounterType.BOUNTY)),
                "{1}, {T}: Put a bounty counter on target creature. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.BOUNTY), ObjectRelation.ANY),
                SequenceEffect.of(
                        new DrawCardEffect(1, ITS_CONTROLLERS_OPPONENTS),
                        new GainLifeEffect(new Fixed(2), ITS_CONTROLLERS_OPPONENTS)));
    }
}
