package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSelfAndCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "63")
public class DreamFighter extends Card {

    public DreamFighter() {
        // Whenever this creature blocks or becomes blocked by a creature, this creature and that
        // creature phase out. The combat opponent is carried as the trigger's (non-targeting) target.
        addTrigger(Triggers.blocks(), new PhaseOutSelfAndCombatOpponentEffect());
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new PhaseOutSelfAndCombatOpponentEffect());
    }
}
