package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

    private final GameBroadcastService gameBroadcastService;
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
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            return;
        }

        int count = graveyard.size();
        deck.addAll(graveyard);
        graveyard.clear();
        graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);

        String logEntry = playerName + " shuffles their graveyard (" + LibraryShuffleSupport.pluralCards(count) + ") into their library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }
}
