package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.GainControlOfCombatOpponentAtEndOfCombatEffect;

@CardRegistration(set = "5ED", collectorNumber = "197")
public class TheWretched extends Card {

    public TheWretched() {
        // At end of combat, gain control of all creatures blocking this creature for as long as you
        // control this creature. One trigger per blocker; control lasts while The Wretched stays.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new GainControlOfCombatOpponentAtEndOfCombatEffect(), TriggerMode.PER_BLOCKER);
    }
}
