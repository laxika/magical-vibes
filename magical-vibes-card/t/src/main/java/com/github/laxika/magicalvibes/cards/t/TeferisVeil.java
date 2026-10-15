package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PhaseOutAttackingCreatureAtEndOfCombatEffect;

@CardRegistration(set = "WTH", collectorNumber = "53")
public class TeferisVeil extends Card {

    public TeferisVeil() {
        // Whenever a creature you control attacks, it phases out at end of combat.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new PhaseOutAttackingCreatureAtEndOfCombatEffect());
    }
}
