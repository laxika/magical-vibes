package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCounterPTEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "81")
public class UnwillingVessel extends Card {

    public UnwillingVessel() {
        PutCountersOnSelfEffect possessionCounter = new PutCountersOnSelfEffect(CounterType.POSSESSION);
        addTrigger(List.of(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                Triggers.youFullyUnlockRoom()), possessionCounter);

        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenWithDyingSourceCounterPTEffect(
                new CreateTokenEffect("Spirit", 0, 0, CardColor.BLUE, List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
