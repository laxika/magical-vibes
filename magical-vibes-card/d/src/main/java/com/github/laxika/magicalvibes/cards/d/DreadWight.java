package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;

@CardRegistration(set = "ICE", collectorNumber = "122")
@CardRegistration(set = "ME4", collectorNumber = "79")
public class DreadWight extends Card {

    public DreadWight() {
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new PutCounterOnCombatOpponentAtEndOfCombatEffect(CounterType.PARALYZATION, 1, true));
    }
}
