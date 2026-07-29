package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PoisonAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPoisonAtNextUpkeepUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Sabertooth Cobra's {@code ON_DAMAGE_TO_PLAYER} trigger: reads the damaged player from the
 * stack entry's {@code targetId} (baked in as {@code DAMAGED_PLAYER}) and queues a
 * {@link PoisonAtNextUpkeepUnlessPays} delayed action, drained at that player's next upkeep.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterPoisonAtNextUpkeepUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterPoisonAtNextUpkeepUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterPoisonAtNextUpkeepUnlessPaysEffect) effect;
        UUID playerId = entry.getTargetId();
        if (playerId == null) return;

        gameData.queueDelayedAction(new PoisonAtNextUpkeepUnlessPays(
                playerId, e.amount(), e.manaCost(), entry.getCard()));

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " must pay " + e.manaCost()
                + " before their next upkeep or get another poison counter. (", entry.getCard(), ")"));
        log.info("Game {} - {} scheduled an upkeep pay-or-poison obligation on {}",
                gameData.id, entry.getCard().getName(), playerName);
    }
}
