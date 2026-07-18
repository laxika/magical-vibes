package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.SirensCallEffect;

@CardRegistration(set = "4ED", collectorNumber = "101")
public class SirensCall extends Card {

    public SirensCall() {
        // Cast this spell only during an opponent's turn, before attackers are declared.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_TURN_BEFORE_ATTACKERS);

        // Creatures the active player controls attack this turn if able; at the beginning of the next
        // end step, destroy all non-Wall creatures that player controls that didn't attack this turn
        // (ignoring creatures they didn't control continuously since the beginning of the turn).
        addEffect(EffectSlot.SPELL, new SirensCallEffect());
    }
}
