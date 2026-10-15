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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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
        if (entry.getTargetId() == null && gameData.currentStep == TurnStep.END_OF_COMBAT) {
            destroyCombatOpponents(gameData, entry, destroyEffect);
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData,
                entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId());
        if (target == null) {
            return;
        }
        Set<UUID> combatOpponentIds = combatOpponentIds(gameData, target.getId(), destroyEffect);
        gameData.queueDelayedAction(new DestroyCombatOpponentsAtEndOfCombat(target.getId(),
                destroyEffect.onlyCreaturesBlockedByTarget(), Set.copyOf(combatOpponentIds),
                entry.getCard(), entry.getControllerId()));
        String affectedCreatures = destroyEffect.onlyCreaturesBlockedByTarget()
                ? "'s blocked creatures will be destroyed at end of combat."
                : "'s combat opponents will be destroyed at end of combat.";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), affectedCreatures));
    }

    /**
     * The end-of-combat delayed trigger: destroys the creature's combat opponents, both those
     * remembered when the delayed trigger was created (carried as the entry's triggering
     * permanents) and those recorded since.
     */
    private void destroyCombatOpponents(GameData gameData, StackEntry entry,
                                        DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect) {
        Set<UUID> opponentIds = new LinkedHashSet<>(entry.getTriggeringPermanentIds());
        opponentIds.addAll(combatOpponentIds(gameData, entry.getSourcePermanentId(), destroyEffect));
        for (UUID opponentId : opponentIds) {
            Permanent opponent = gameQueryService.findPermanentById(gameData, opponentId);
            if (opponent != null && permanentRemovalService.tryDestroyPermanent(gameData, opponent)) {
                gameLogService.append(gameData, GameLog.isDestroyed(opponent.getCard()));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private static Set<UUID> combatOpponentIds(GameData gameData, UUID creatureId,
                                               DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect) {
        if (creatureId == null) {
            return Set.of();
        }
        return destroyEffect.onlyCreaturesBlockedByTarget()
                ? gameData.combatOpponentIdsBlockedByThisTurn.getOrDefault(creatureId, Set.of())
                : gameData.combatBlockOpponentIdsThisTurn.getOrDefault(creatureId, Set.of());
    }
}
