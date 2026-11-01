package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "1")
public class DinaEssenceBrewer extends Card {

    public DinaEssenceBrewer() {
        // Whenever you sacrifice a creature, draw a card. This ability triggers only once each turn.
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                new OncePerTurnTriggerEffect(new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new DrawCardEffect(1)
                )));

        // {2}, {T}, Sacrifice another creature: You gain X life and put X +1/+1 counters on target
        // creature you control, where X is the sacrificed creature's power.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(false, true, false, true),
                        new GainLifeEffect(new XValue()),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())
                ),
                "{2}, {T}, Sacrifice another creature: You gain X life and put X +1/+1 counters on target creature you control, where X is the sacrificed creature's power.",
                TargetFilters.creatureYouControl()
        ));
    }
}
