package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "130")
@CardRegistration(set = "2XM", collectorNumber = "117")
@CardRegistration(set = "ECC", collectorNumber = "89")
@CardRegistration(set = "TLE", collectorNumber = "26")
@CardRegistration(set = "TMC", collectorNumber = "47")
@CardRegistration(set = "C14", collectorNumber = "172")
public class BlasphemousAct extends Card {

    public BlasphemousAct() {
        // Blasphemous Act costs {1} less to cast for each creature on the battlefield.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(13));
    }
}
