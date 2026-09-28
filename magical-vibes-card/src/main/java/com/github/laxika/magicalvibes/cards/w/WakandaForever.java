package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect;

@CardRegistration(set = "MSC", collectorNumber = "71")
@CardRegistration(set = "MSC", collectorNumber = "388")
public class WakandaForever extends Card {

    public WakandaForever() {
        addEffect(EffectSlot.SPELL,
                new LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect(
                        6, new EnterWithCountersEffect(CounterType.INDESTRUCTIBLE, new Fixed(1))));
    }
}
