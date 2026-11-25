package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "68")
public class ThopterMechanic extends Card {

    public ThopterMechanic() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new CreateTokenEffect("Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));
    }
}
