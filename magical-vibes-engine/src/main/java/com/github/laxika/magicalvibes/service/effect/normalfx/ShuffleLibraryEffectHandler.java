package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Untargeted shuffles always affect the controller; only the targeted form reads targetId.
        ShuffleLibraryEffect shuffleEffect = (ShuffleLibraryEffect) effect;
        UUID playerId = shuffleEffect.targetPlayer() ? entry.getTargetId() : entry.getControllerId();
        String playerName = gameData.playerIdToName.get(playerId);

        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

        String logEntry = playerName + " shuffles their library.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} shuffles their library", gameData.id, playerName);
    }
}
