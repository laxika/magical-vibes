package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Resolves {@link DestroyCombatOpponentsOfTargetAtEndOfCombatEffect} by queueing a
 * {@link DestroyCombatOpponentsAtEndOfCombat} delayed action for the target. The creatures to
 * destroy are captured when the spell or ability resolves.
 */
@Component
@RequiredArgsConstructor
public class DestroyCombatOpponentsOfTargetAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCombatOpponentsOfTargetAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect =
                (DestroyCombatOpponentsOfTargetAtEndOfCombatEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData,
                entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId());
        if (target == null) {
            return;
        }
        Set<java.util.UUID> combatOpponentIds = destroyEffect.onlyCreaturesBlockedByTarget()
                ? gameData.combatOpponentIdsBlockedByThisTurn.getOrDefault(target.getId(), Set.of())
                : gameData.combatBlockOpponentIdsThisCombat.getOrDefault(target.getId(), Set.of());
        if (entry.getTargetId() == null && gameData.currentStep == TurnStep.END_OF_COMBAT) {
            for (java.util.UUID combatOpponentId : Set.copyOf(combatOpponentIds)) {
                Permanent opponent = gameQueryService.findPermanentById(gameData, combatOpponentId);
                if (opponent != null && permanentRemovalService.tryDestroyPermanent(gameData, opponent)) {
                    gameLogService.append(gameData, GameLog.isDestroyed(opponent.getCard()));
                }
            }
            return;
        }
        gameData.queueDelayedAction(new DestroyCombatOpponentsAtEndOfCombat(target.getId(),
                destroyEffect.onlyCreaturesBlockedByTarget(), Set.copyOf(combatOpponentIds)));
        String affectedCreatures = destroyEffect.onlyCreaturesBlockedByTarget()
                ? "'s blocked creatures will be destroyed at end of combat."
                : "'s combat opponents will be destroyed at end of combat.";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), affectedCreatures));
    }
}
