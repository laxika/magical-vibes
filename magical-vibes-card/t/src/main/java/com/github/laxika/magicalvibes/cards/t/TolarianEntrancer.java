package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "56")
public class TolarianEntrancer extends Card {

    public TolarianEntrancer() {
        // Whenever this creature becomes blocked by a creature, gain control of that creature at
        // end of combat. One trigger per blocker; the control gain has no duration, so it is
        // permanent and happens even if the Entrancer dies in combat first.
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new GainControlOfCombatOpponentAtEndOfCombatEffect(ControlDuration.PERMANENT));
    }
}
