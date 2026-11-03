package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "65")
@CardRegistration(set = "ECL", collectorNumber = "358")
public class RimefireTorque extends Card {

    public RimefireTorque() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addTrigger(Triggers.enters(new PermanentHasSourceChosenSubtypePredicate(), ObjectRelation.YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.CHARGE),
                        new CopyNextInstantOrSorceryCastThisTurnEffect()),
                "{T}, Remove three charge counters from Rimefire Torque: When you next cast an instant "
                        + "or sorcery spell this turn, copy it. You may choose new targets for the copy."));
    }
}
