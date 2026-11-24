package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Each player the effect names shuffles their library, in APNAP order. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ShuffleLibraryEffect shuffleEffect = (ShuffleLibraryEffect) effect;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, shuffleEffect.players())) {
            String playerName = gameData.playerIdToName.get(playerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " shuffles their library."));
            log.info("Game {} - {} shuffles their library", gameData.id, playerName);
        }
    }
}
