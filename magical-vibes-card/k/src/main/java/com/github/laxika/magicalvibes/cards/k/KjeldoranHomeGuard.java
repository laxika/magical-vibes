package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisCombat;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "8")
@CardRegistration(set = "CST", collectorNumber = "3")
@CardRegistration(set = "CST", collectorNumber = "8")
@CardRegistration(set = "ME2", collectorNumber = "22")
public class KjeldoranHomeGuard extends Card {

    private static final CreateTokenEffect DESERTER_TOKEN = new CreateTokenEffect(
            "Deserter", 0, 1, CardColor.WHITE, List.of(CardSubtype.DESERTER), Set.of(), Set.<CardType>of());

    public KjeldoranHomeGuard() {
        // At end of combat, if this creature attacked or blocked this combat, put a -0/-1 counter on
        // this creature and create a 0/1 white Deserter creature token.
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED, new ConditionalEffect(new SourceAttackedOrBlockedThisCombat(),
                new PutCountersOnSelfEffect(CounterType.MINUS_ZERO_MINUS_ONE)));
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED, new ConditionalEffect(new SourceAttackedOrBlockedThisCombat(),
                DESERTER_TOKEN));
    }
}
