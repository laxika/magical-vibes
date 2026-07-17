package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GameOfChaosEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GameOfChaosEffect}: performs the first coin flip (stakes 1) against the targeted
 * opponent, then queues the repeating "flip again?" prompt (doubled stakes) for the flip's winner.
 * Subsequent flips are driven by {@code GameOfChaosFlipAgainHandler}.
 */
@Component
@RequiredArgsConstructor
public class GameOfChaosEffectHandler implements NormalEffectHandlerBean {

    private final GameOfChaosSupport gameOfChaosSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GameOfChaosEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (!gameData.playerIds.contains(opponentId)) {
            return;
        }

        String sourceName = entry.getCard().getName();
        UUID decidingPlayer = gameOfChaosSupport.flipRound(gameData, sourceName, controllerId, opponentId, 1);
        if (decidingPlayer == null) {
            return;
        }
        gameOfChaosSupport.queueFlipAgain(gameData, entry.getCard(), controllerId, opponentId, decidingPlayer, 2);
    }
}
