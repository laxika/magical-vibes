package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TurfWarCombatDamageEffect;

@CardRegistration(set = "NCC", collectorNumber = "54")
@CardRegistration(set = "NCC", collectorNumber = "154")
public class TurfWar extends Card {

    public TurfWar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerChoosesLandAndPutCounterEffect(CounterType.CONTESTED));
        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new TurfWarCombatDamageEffect(true));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
                new TurfWarCombatDamageEffect(false));
    }
}
