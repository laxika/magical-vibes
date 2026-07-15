package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOpponentPaysLifeOrToHandEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardsOpponentPaysLifeOrToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final LibraryRevealSupport libraryRevealSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsOpponentPaysLifeOrToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsOpponentPaysLifeOrToHandEffect e = (RevealTopCardsOpponentPaysLifeOrToHandEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();

        // Broadcast the reveal with all card names
        String revealedNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + "."));

        // If the opponent doesn't have enough life to pay for even one card, all go to hand
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();
        int opponentLife = gameData.playerLifeTotals.get(opponentId);

        if (opponentLife < e.lifeCost()) {
            // Opponent can't afford to pay for even one card
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            String handNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + handNames + " into their hand."));
            log.info("Game {} - {} resolving {} — all {} cards to hand (opponent can't afford to pay)",
                    gameData.id, playerName, cardName, topCards.size());
            return;
        }

        // Present all revealed cards to the opponent — they select which ones to exile (paying life each)
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.beginWithoutPrompt(gameData, new PendingInteraction.LibraryRevealChoice(
                opponentId, new ArrayList<>(topCards), cardIds,
                false, true, false, false, false, e.lifeCost(), controllerId, topCards.size(),
                "Choose cards to deny (you pay " + e.lifeCost() + " life for each). Unselected cards go to opponent's hand."));

        gameBroadcastService.broadcastGameState(gameData);

        interactionHandlerRegistry.promptActive(gameData);

        log.info("Game {} - {} reveals {} cards for {}, opponent must choose which to deny",
                gameData.id, playerName, topCards.size(), cardName);
    
    }
}
