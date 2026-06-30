package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndGraveyardIntoLibraryEffect;
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
public class EachPlayerShufflesHandAndGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesHandAndGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            int handCount = (hand != null) ? hand.size() : 0;
            int graveyardCount = (graveyard != null) ? graveyard.size() : 0;

            if (hand != null && !hand.isEmpty()) {
                deck.addAll(hand);
                hand.clear();
            }

            if (graveyard != null && !graveyard.isEmpty()) {
                deck.addAll(graveyard);
                graveyard.clear();
                graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
            }

            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " shuffles their hand (" + LibraryShuffleSupport.pluralCards(handCount)
                            + ") and graveyard (" + LibraryShuffleSupport.pluralCards(graveyardCount)
                            + ") into their library (" + cardName + ").");
            log.info("Game {} - {} shuffles hand ({}) and graveyard ({}) into library ({})",
                    gameData.id, playerName, handCount, graveyardCount, cardName);
        }
    }
}
