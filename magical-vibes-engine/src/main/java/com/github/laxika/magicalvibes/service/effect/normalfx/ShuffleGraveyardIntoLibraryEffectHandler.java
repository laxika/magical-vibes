package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class ShuffleGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Fall back to controller when no explicit target (e.g. saga chapters, triggered abilities)
        UUID targetPlayerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        // Tokens in the graveyard cease to exist rather than travel (CR 111.7), so a graveyard
        // holding nothing else moves no cards at all.
        List<Card> moving = graveyardService.takeGraveyardCardsForZoneChange(gameData, targetPlayerId);
        if (moving.isEmpty()) {
            String logEntry = playerName + "'s graveyard is empty. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            return;
        }

        int count = moving.size();
        deck.addAll(moving);
        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);

        String logEntry = playerName + " shuffles their graveyard (" + LibraryShuffleSupport.pluralCards(count) + ") into their library.";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }
}
