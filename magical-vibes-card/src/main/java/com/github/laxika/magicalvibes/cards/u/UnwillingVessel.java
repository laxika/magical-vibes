package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCounterPTEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "81")
public class UnwillingVessel extends Card {

    public UnwillingVessel() {
        PutCountersOnSelfEffect possessionCounter = new PutCountersOnSelfEffect(CounterType.POSSESSION);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, possessionCounter);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, possessionCounter);

        addEffect(EffectSlot.ON_DEATH, new CreateTokenWithDyingSourceCounterPTEffect(
                new CreateTokenEffect("Spirit", 0, 0, CardColor.BLUE, List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
