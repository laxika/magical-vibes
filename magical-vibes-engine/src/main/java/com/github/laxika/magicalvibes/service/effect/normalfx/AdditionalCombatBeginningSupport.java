package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OnlyLandCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.OnlySourceCreatureCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyTargetCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * What an effect that adds combat phases does about "that combat". The phases it adds come directly after the phase it
 * resolved in, before the ones created earlier (CR 500.8), so whatever waits for one of those later combat phases
 * waits for a later one. A triggered effect "at the beginning of that combat" becomes a delayed triggered ability about
 * the added combat phase alone; a restriction on what can attack in the added combat ("only land creatures can attack
 * during that combat") is not a triggered ability but a floating effect of the resolution (CR 611.2a) that applies
 * during that combat phase ({@link Duration#duringCombatPhase}).
 */
final class AdditionalCombatBeginningSupport {

    private AdditionalCombatBeginningSupport() {
    }

    /**
     * Whether {@code effect} restricts which creatures can attack in an added combat, a rules change rather than a
     * triggered ability.
     */
    static boolean restrictsAttackers(CardEffect effect) {
        return effect instanceof OnlyLandCreaturesCanAttackThisCombatEffect
                || effect instanceof OnlySourceCreatureCanAttackThisCombatEffect
                || effect instanceof OnlyTargetCreaturesCanAttackThisCombatEffect;
    }

    /**
     * Adds {@code count} combat phases for {@code entry}'s resolution, the first of them after
     * {@code combatPhasesBefore} more combat phases have begun, and registers each of {@code beginningEffects} for each
     * of them. Returns the number of the first added combat phase this turn.
     */
    static int register(GameData gameData, StackEntry entry, List<CardEffect> beginningEffects, int count,
                        int combatPhasesBefore) {
        int first = gameData.combatPhasesThisTurn + 1 + combatPhasesBefore;
        synchronized (gameData.temporaryGlobalTriggeredAbilities) {
            gameData.temporaryGlobalTriggeredAbilities.replaceAll(delayed ->
                    delayed.referents().combatPhase() >= first
                            ? delayed.withReferents(delayed.referents()
                            .duringCombatPhase(delayed.referents().combatPhase() + count))
                            : delayed);
        }
        gameData.replaceScheduledFloatingEffects(scheduled -> scheduled.duration().start()
                        == Duration.Start.COMBAT_PHASE_OF_THIS_TURN && scheduled.duration().combatPhase() >= first,
                scheduled -> scheduled.withDuration(scheduled.duration().laterCombatPhase(count)));
        for (CardEffect beginningEffect : beginningEffects) {
            for (int i = 0; i < count; i++) {
                if (restrictsAttackers(beginningEffect)) {
                    gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                            entry.getSourcePermanentId(), entry.getControllerId(), beginningEffect, null,
                            Duration.duringCombatPhase(first + i)));
                } else {
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepThisTurn(
                            Triggers.beginningOfCombat(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                            entry.getSourcePermanentId(), null, DelayedReferents.NONE.duringCombatPhase(first + i),
                            beginningEffect));
                }
            }
        }
        return first;
    }
}
