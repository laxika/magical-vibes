package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "117")
public class RetchedWretch extends Card {

    public RetchedWretch() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, true)));
    }
}
