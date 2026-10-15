package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Set;

/**
 * Resolves the "At end of combat" trigger carrying {@link TapCombatOpponentAtEndOfCombatEffect}:
 * taps each creature that blocked the source this turn and keeps it from untapping during its
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
        Set<UUID> blockerIds = gameData.combatBlockOpponentIdsThisTurn
                .getOrDefault(entry.getSourcePermanentId(), Set.of());
        for (UUID blockerId : blockerIds) {
            tapAndLock(gameData, blockerId);
        }
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
