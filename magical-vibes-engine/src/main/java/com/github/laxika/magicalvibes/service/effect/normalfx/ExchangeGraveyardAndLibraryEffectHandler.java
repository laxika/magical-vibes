package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeGraveyardAndLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeGraveyardAndLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeGraveyardAndLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(playerId);
        List<Card> oldLibrary = new ArrayList<>(library);

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            List<Card> oldGraveyard = graveyardService.takeGraveyardCardsForZoneChange(gameData, playerId);
            library.clear();
            library.addAll(oldGraveyard);
            graveyardService.addCardsFromLibraryToGraveyard(gameData, playerId, oldLibrary);
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exchanges their graveyard and library."));
        log.info("Game {} - {} exchanges their graveyard and library ({} cards moved from library)",
                gameData.id, playerName, oldLibrary.size());
    }
}
