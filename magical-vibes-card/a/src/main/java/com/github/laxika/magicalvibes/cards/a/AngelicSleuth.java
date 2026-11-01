package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "12")
@CardRegistration(set = "NCC", collectorNumber = "113")
@CardRegistration(set = "FDC", collectorNumber = "16")
public class AngelicSleuth extends Card {

    public AngelicSleuth() {
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        CreateTokenEffect.ofClueToken(1)));
    }
}
