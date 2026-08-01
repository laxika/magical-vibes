package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleSelfAndGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleSelfAndGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        // Move source permanent from battlefield to library (if still there)
        boolean selfShuffled = false;
        if (entry.getSourcePermanentId() != null) {
            Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (self != null) {
                permanentRemovalService.removePermanentToLibraryBottom(gameData, self);
                permanentRemovalService.removeOrphanedAuras(gameData);
                selfShuffled = true;
            }
        }

        // Move all graveyard cards into library. Tokens cease to exist rather than travel (CR 111.7).
        List<Card> moving = graveyardService.takeGraveyardCardsForZoneChange(gameData, controllerId);
        int graveyardCount = moving.size();
        deck.addAll(moving);

        // Shuffle the library
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        // Log
        if (selfShuffled && graveyardCount > 0) {
            
            gameLogService.append(gameData, GameLog.builder().text(playerName + " shuffles ").card(entry.getCard()).text(" and their graveyard (" + LibraryShuffleSupport.pluralCards(graveyardCount) + ") into their library.").build());
        } else if (selfShuffled) {
            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " shuffles " , entry.getCard(), " into their library."));
        } else if (graveyardCount > 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " shuffles their graveyard ("
                    + LibraryShuffleSupport.pluralCards(graveyardCount) + ") into their library."));
        } else {
            String logEntry = playerName + " shuffles their library.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        }

        log.info("Game {} - {} shuffles self={} + graveyard ({} cards) into library",
                gameData.id, playerName, selfShuffled, graveyardCount);
    }
}
