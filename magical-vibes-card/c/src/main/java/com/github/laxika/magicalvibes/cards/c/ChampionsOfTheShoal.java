package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "46")
@CardRegistration(set = "ECL", collectorNumber = "356")
public class ChampionsOfTheShoal extends Card {

    public ChampionsOfTheShoal() {
        addCastingCost(new BeholdAndExileCost(CardSubtype.MERFOLK));

        var tapAndStun = SequenceEffect.of(
                new TapPermanentsEffect(Subjects.target()),
                new PutCountersEffect(CounterType.STUN));
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(Subjects.target()),
                        new PutCountersEffect(CounterType.STUN))
                .addTrigger(Triggers.becomesTapped(), tapAndStun);
    }
}
