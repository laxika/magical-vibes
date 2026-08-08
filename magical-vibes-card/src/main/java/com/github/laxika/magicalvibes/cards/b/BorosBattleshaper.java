package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Boros Battleshaper — the two "up to one target creature" halves are independent target groups, so
 * either may be declined and the same creature can't be chosen for both.
 *
 * <p>The lock half is stamped for the rest of the turn rather than for the combat only; combat-scoped
 * floating expiry is not plumbed yet, and the two durations differ only in a turn with extra combat
 * phases.</p>
 */
@CardRegistration(set = "DGM", collectorNumber = "58")
public class BorosBattleshaper extends Card {

    public BorosBattleshaper() {
        // At the beginning of each combat, up to one target creature attacks or blocks this combat
        // if able and up to one target creature can't attack or block this combat.
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK_OR_BLOCK));

        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                        new LockTargetPermanentEffect(true, true, false, EffectDuration.UNTIL_END_OF_TURN));
    }
}
