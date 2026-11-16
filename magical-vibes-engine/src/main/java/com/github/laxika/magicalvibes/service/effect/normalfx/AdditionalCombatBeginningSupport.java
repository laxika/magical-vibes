package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IllusionistsGambitAdditionalCombatEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyLandCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.OnlySourceCreatureCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyTargetCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * What an effect that adds combat phases does about "at the beginning of that combat": a triggered effect becomes a
 * delayed triggered ability about the added combat phase alone, and a restriction on what can attack in the added
 * combat ("only land creatures can attack during that combat") is not a triggered ability, so it is kept for the
 * added combat to apply as it begins.
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
                || effect instanceof OnlyTargetCreaturesCanAttackThisCombatEffect
                || effect instanceof IllusionistsGambitAdditionalCombatEffect;
    }

    /**
     * Registers {@code beginningEffect} for each of {@code count} combat phases {@code entry}'s resolution adds, the
     * first of them after {@code combatPhasesBefore} more combat phases have begun. The phases it adds come directly
     * after the phase it resolved in, before the ones created earlier (CR 500.8), so each of those waiting for a combat
     * phase from the first added one on waits for a later one.
     */
    static void register(GameData gameData, StackEntry entry, CardEffect beginningEffect, int count,
                         int combatPhasesBefore) {
        if (restrictsAttackers(beginningEffect)) {
            for (int i = 0; i < count; i++) {
                gameData.queueDelayedAction(new DelayedAdditionalCombatBeginningEffect(entry.getControllerId(),
                        entry.getCard(), beginningEffect, entry.getSourcePermanentId()));
            }
            return;
        }
        int first = gameData.combatPhasesThisTurn + 1 + combatPhasesBefore;
        synchronized (gameData.temporaryGlobalTriggeredAbilities) {
            gameData.temporaryGlobalTriggeredAbilities.replaceAll(delayed ->
                    delayed.referents().combatPhase() >= first
                            ? delayed.withReferents(delayed.referents()
                            .duringCombatPhase(delayed.referents().combatPhase() + count))
                            : delayed);
        }
        for (int i = 0; i < count; i++) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepThisTurn(
                    Triggers.beginningOfCombat(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                    entry.getSourcePermanentId(), null, DelayedReferents.NONE.duringCombatPhase(first + i),
                    beginningEffect));
        }
    }
}
