package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "145")
@CardRegistration(set = "ME4", collectorNumber = "90")
public class LimDLsCohort extends Card {

    public LimDLsCohort() {
        // Whenever this creature blocks or becomes blocked by a creature, that creature
        // can't be regenerated this turn. The combat opponent is carried as the trigger's
        // (non-targeting) target.
        addTrigger(Triggers.blocks(), new PreventTargetCreatureRegenerationThisTurnEffect());
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new PreventTargetCreatureRegenerationThisTurnEffect());
    }
}
