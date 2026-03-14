package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerCreatureOnBattlefieldEffect;

@CardRegistration(set = "ISD", collectorNumber = "130")
public class BlasphemousAct extends Card {

    public BlasphemousAct() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostPerCreatureOnBattlefieldEffect(1));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(13));
    }
}
