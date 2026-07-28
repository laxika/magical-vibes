package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ChooseBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect;

/**
 * Melee — "Cast this spell only during combat on your turn before blockers are declared. You choose
 * which creatures block this combat and how those creatures block. Whenever a creature attacks and
 * isn't blocked this combat, untap it and remove it from combat."
 */
@CardRegistration(set = "ICE", collectorNumber = "200")
public class Melee extends Card {

    public Melee() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.YOUR_COMBAT_BEFORE_BLOCKERS);
        addEffect(EffectSlot.SPELL, new ChooseBlockersThisCombatEffect());
        addEffect(EffectSlot.SPELL, new RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect());
    }
}
