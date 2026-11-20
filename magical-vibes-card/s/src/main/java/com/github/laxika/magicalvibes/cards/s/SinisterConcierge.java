package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersAndGainSuspendEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "30")
@CardRegistration(set = "NCC", collectorNumber = "131")
public class SinisterConcierge extends Card {

    public SinisterConcierge() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                        new MayEffect(new ExileSourceCardFromGraveyardEffect(),
                                "Exile Sinister Concierge with three time counters?"),
                        PutTimeCountersAndGainSuspendEffect.onCardsExiledThisWay(3),
                        new ConditionalEffect(new SourceCardSuspended(), SequenceEffect.of(
                                new ExileTargetPermanentEffect(),
                                PutTimeCountersAndGainSuspendEffect.onCardsExiledThisWay(3)))));
    }
}
