package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerShufflesHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a hand-and-graveyard shuffle for every player other than the entry controller. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachOtherPlayerShufflesHandAndGraveyardIntoLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ZoneToLibraryService zoneToLibraryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOtherPlayerShufflesHandAndGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }

            String playerName = gameData.playerIdToName.get(playerId);
            ZoneToLibraryService.MovedCounts moved =
                    zoneToLibraryService.moveHandAndGraveyardIntoLibrary(gameData, playerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameLogService.append(gameData, GameLog.text(playerName + " shuffles their hand ("
                    + LibraryShuffleSupport.pluralCards(moved.hand()) + ") and graveyard ("
                    + LibraryShuffleSupport.pluralCards(moved.graveyard())
                    + ") into their library (" + entry.getCard().getName() + ")."));
            log.info("Game {} - {} shuffles hand ({}) and graveyard ({}) into library ({})",
                    gameData.id, playerName, moved.hand(), moved.graveyard(), entry.getCard().getName());
        }
    }
}
