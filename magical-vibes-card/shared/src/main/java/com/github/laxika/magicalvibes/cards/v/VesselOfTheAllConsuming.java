package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfSourceDealtDamageToPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

public class VesselOfTheAllConsuming extends Card {

    public VesselOfTheAllConsuming() {
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new LoseGameIfSourceDealtDamageToPlayerThisTurnEffect(10));
    }
}
