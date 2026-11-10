package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Boros Battleshaper — the two "up to one target creature" halves are independent target groups, so
 * either may be declined and the same creature may be chosen for both.
 */
@CardRegistration(set = "DGM", collectorNumber = "58")
public class BorosBattleshaper extends Card {

    public BorosBattleshaper() {
        setAllowSharedTargets(true);
        // At the beginning of each combat, up to one target creature attacks or blocks this combat
        // if able and up to one target creature can't attack or block this combat: one ability of two target groups.
        CardEffect attacksOrBlocks = SequenceEffect.of(
                new GrantStaticEffectToTargetUntilEndOfCombatEffect(new MustAttackEffect()),
                new GrantStaticEffectToTargetUntilEndOfCombatEffect(new MustBlockEachCombatEffect()));
        CardEffect cantAttackOrBlock = new LockTargetPermanentEffect(true, true, false,
                EffectDuration.UNTIL_END_OF_COMBAT);
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY), attacksOrBlocks, cantAttackOrBlock);
        target(TargetFilters.creature(), 0, 1).bindEffect(attacksOrBlocks);
        target(TargetFilters.creature(), 0, 1).bindEffect(cantAttackOrBlock);
    }
}
