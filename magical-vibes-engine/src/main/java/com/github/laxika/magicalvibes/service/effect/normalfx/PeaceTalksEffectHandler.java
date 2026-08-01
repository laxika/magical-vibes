package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PeaceTalksEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PeaceTalksEffect} by setting {@link GameData#peaceTalksTurnsRemaining} to at least
 * 2 (current turn + next turn). Decrement happens in {@code TurnProgressionService.advanceTurn}.
 * A second cast while still active refreshes the window to two full turns from resolution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeaceTalksEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PeaceTalksEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.peaceTalksTurnsRemaining = Math.max(gameData.peaceTalksTurnsRemaining, 2);
        gameLogService.append(gameData, GameLog.text(
                "This turn and next turn, creatures can't attack, and players and permanents "
                        + "can't be the targets of spells or activated abilities."));
        log.info("Game {} - Peace Talks active for {} turn(s) including the current one",
                gameData.id, gameData.peaceTalksTurnsRemaining);
    }
}
