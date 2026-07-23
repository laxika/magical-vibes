package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;

@CardRegistration(set = "ICE", collectorNumber = "122")
public class DreadWight extends Card {

    public DreadWight() {
        // At end of combat, put a paralyzation counter on each creature blocking or blocked by this
        // creature and tap those creatures. Creatures with a paralyzation counter don't untap
        // during their controller's untap step (engine-side counter rule) and gain
        // "{4}: Remove a paralyzation counter from this creature" (granted on placement).
        PutCounterOnCombatOpponentAtEndOfCombatEffect paralyzation =
                new PutCounterOnCombatOpponentAtEndOfCombatEffect(CounterType.PARALYZATION, 1, true);
        addEffect(EffectSlot.ON_BLOCK, paralyzation);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, paralyzation, TriggerMode.PER_BLOCKER);
    }
}
