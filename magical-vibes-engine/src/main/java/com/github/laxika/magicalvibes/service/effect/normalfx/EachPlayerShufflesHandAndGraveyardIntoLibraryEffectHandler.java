package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerShufflesHandAndGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ZoneToLibraryService zoneToLibraryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesHandAndGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            String playerName = gameData.playerIdToName.get(playerId);

            ZoneToLibraryService.MovedCounts moved =
                    zoneToLibraryService.moveHandAndGraveyardIntoLibrary(gameData, playerId);

            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameLogService.append(gameData, GameLog.text(playerName + " shuffles their hand (" + LibraryShuffleSupport.pluralCards(moved.hand())
                            + ") and graveyard (" + LibraryShuffleSupport.pluralCards(moved.graveyard())
                            + ") into their library (" + cardName + ")."));
            log.info("Game {} - {} shuffles hand ({}) and graveyard ({}) into library ({})",
                    gameData.id, playerName, moved.hand(), moved.graveyard(), cardName);
        }
    }
}
