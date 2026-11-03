package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "88")
@CardRegistration(set = "OTC", collectorNumber = "149")
public class TenuredInkcaster extends Card {

    public TenuredInkcaster() {
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)),
                SequenceEffect.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT), new GainLifeEffect(1)));
    }
}
