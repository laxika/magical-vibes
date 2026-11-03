package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "85")
public class DreamSpoilers extends Card {

    public DreamSpoilers() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.youCastSpell().duringOpponentsTurn(), new BoostTargetCreatureEffect(-1, -1));
    }
}
