package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneCounterOnSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "269")
public class WickerWarcrawler extends Card {

    public WickerWarcrawler() {
        // Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of combat.
        addEffect(EffectSlot.ON_ATTACK, new PutMinusOneCounterOnSourceAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new PutMinusOneCounterOnSourceAtEndOfCombatEffect());
    }
}
