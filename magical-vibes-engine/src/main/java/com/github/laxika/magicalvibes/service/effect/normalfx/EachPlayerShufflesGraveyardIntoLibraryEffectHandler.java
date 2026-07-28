package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesGraveyardIntoLibraryEffect;
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
public class EachPlayerShufflesGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            // Tokens in the graveyard cease to exist rather than travel (CR 111.7), so a graveyard
            // holding nothing else moves no cards at all.
            List<Card> moving = graveyardService.takeGraveyardCardsForZoneChange(gameData, playerId);
            if (moving.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s graveyard is empty. Library is shuffled."));
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                continue;
            }

            int count = moving.size();
            deck.addAll(moving);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameLogService.append(gameData, GameLog.text(
                    playerName + " shuffles their graveyard ("
                            + LibraryShuffleSupport.pluralCards(count) + ") into their library."));
            log.info("Game {} - {} shuffles graveyard ({} cards) into library",
                    gameData.id, playerName, count);
        }
    }
}
