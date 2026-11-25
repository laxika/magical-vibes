package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "246")
@CardRegistration(set = "FIN", collectorNumber = "512")
public class TidusBlitzballStar extends Card {

    public TidusBlitzballStar() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.attacks(), new TapPermanentsEffect(Subjects.target()));
    }
}
