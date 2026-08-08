package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Timetwister-style mass reset: every player's hand and graveyard — plus, for Sway of the Stars,
 * every permanent they own — goes into their library, which is then shuffled.
 *
 * <p>The permanent sweep is a separate first pass over all players so the battlefield empties as
 * one event rather than one player at a time; only after that does each library get drained and
 * shuffled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerShufflesZonesIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ZoneToLibraryService zoneToLibraryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesZonesIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();
        boolean includePermanents = ((EachPlayerShufflesZonesIntoLibraryEffect) effect).includeOwnedPermanents();

        if (includePermanents) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                int moved = zoneToLibraryService.moveOwnedPermanentsIntoLibrary(gameData, playerId);
                if (moved > 0) {
                    gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                            + " shuffles " + LibraryShuffleSupport.pluralPermanents(moved)
                            + " they own into their library (" + cardName + ")."));
                }
            }
        }

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
