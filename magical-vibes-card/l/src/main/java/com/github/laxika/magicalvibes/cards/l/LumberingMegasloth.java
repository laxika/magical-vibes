package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalCountersAmongPlayersAndPermanents;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "PIP", collectorNumber = "80")
@CardRegistration(set = "PIP", collectorNumber = "608")
public class LumberingMegasloth extends Card {

    public LumberingMegasloth() {
        // This spell costs {1} less to cast for each counter among players and permanents.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new TotalCountersAmongPlayersAndPermanents()));

        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
