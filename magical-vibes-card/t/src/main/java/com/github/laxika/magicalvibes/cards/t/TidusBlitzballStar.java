package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "246")
@CardRegistration(set = "FIN", collectorNumber = "512")
public class TidusBlitzballStar extends Card {

    public TidusBlitzballStar() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ATTACK, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
