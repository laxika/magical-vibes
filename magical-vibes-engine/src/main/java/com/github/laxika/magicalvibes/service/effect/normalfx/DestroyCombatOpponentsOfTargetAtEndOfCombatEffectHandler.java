package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCombatOpponentsOfTargetAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect =
                (DestroyCombatOpponentsOfTargetAtEndOfCombatEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        Set<java.util.UUID> combatOpponentIds = destroyEffect.onlyCreaturesBlockedByTarget()
                ? gameData.combatOpponentIdsBlockedByThisTurn.getOrDefault(target.getId(), Set.of())
                : gameData.combatBlockOpponentIdsThisTurn.getOrDefault(target.getId(), Set.of());
        gameData.queueDelayedAction(new DestroyCombatOpponentsAtEndOfCombat(target.getId(),
                destroyEffect.onlyCreaturesBlockedByTarget(), Set.copyOf(combatOpponentIds)));
        String affectedCreatures = destroyEffect.onlyCreaturesBlockedByTarget()
                ? "'s blocked creatures will be destroyed at end of combat."
                : "'s combat opponents will be destroyed at end of combat.";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), affectedCreatures));
    }
}
