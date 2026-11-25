package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "53")
@CardRegistration(set = "WHO", collectorNumber = "658")
public class RenegadeSilent extends Card {

    public RenegadeSilent() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new GoadTargetCreatureUntilNextTurnEffect(),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new PhaseOutEffect(Subjects.source())));
    }
}
