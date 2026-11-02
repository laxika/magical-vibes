package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutAttackingCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "53")
public class TeferisVeil extends Card {

    public TeferisVeil() {
        // Whenever a creature you control attacks, it phases out at end of combat.
        addTrigger(Triggers.creatureYouControlAttacks(), new PhaseOutAttackingCreatureAtEndOfCombatEffect());
    }
}
