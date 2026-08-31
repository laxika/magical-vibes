package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.TapAndSkipUntapAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Set;

/**
 * Resolves {@link TapCombatOpponentAtEndOfCombatEffect}: schedules the referenced combat opponent
 * (carried as the stack entry's non-targeting target) to be tapped at end of combat and to skip its
 * controller's next untap step. See Joven's Ferrets.
 */
@Component
@RequiredArgsConstructor
public class TapCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null && gameData.currentStep == TurnStep.END_OF_COMBAT) {
            Set<UUID> blockerIds = gameData.combatBlockOpponentIdsThisTurn
                    .getOrDefault(entry.getSourcePermanentId(), Set.of());
            for (UUID blockerId : blockerIds) {
                tapAndLock(gameData, blockerId);
            }
            return;
        }
        if (targetId == null) {
            return;
        }
        if (gameData.currentStep == TurnStep.END_OF_COMBAT) {
            tapAndLock(gameData, targetId);
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        gameData.queueDelayedAction(new TapAndSkipUntapAtEndOfCombat(targetId));
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " will be tapped at end of combat."));
    }

    private void tapAndLock(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        tapUntapSupport.tapPermanent(gameData, target);
        target.setSkipUntapCount(Math.max(target.getSkipUntapCount(), 1));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " is tapped and doesn't untap during its controller's next untap step."));
    }
}
