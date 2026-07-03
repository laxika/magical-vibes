package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KarnScionRevealTwoOpponentChoosesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KarnScionRevealTwoOpponentChoosesEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KarnScionRevealTwoOpponentChoosesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck.size() < 2) {
            // If fewer than 2 cards, reveal what's available
            if (deck.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData,
                        controllerName + "'s library is empty — nothing to reveal.");
                log.info("Game {} - {} has no cards to reveal for Karn Scion +1", gameData.id, controllerName);
                return;
            }
            // Only 1 card: it goes to hand (no opponent choice needed), nothing to exile
            Card onlyCard = deck.removeFirst();
            gameData.addCardToHand(controllerId, onlyCard);
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " reveals " + onlyCard.getName() + " and puts it into their hand.");
            log.info("Game {} - {} reveals single card {} for Karn Scion +1", gameData.id, controllerName, onlyCard.getName());
            return;
        }

        Card card1 = deck.removeFirst();
        Card card2 = deck.removeFirst();
        List<Card> revealedCards = List.of(card1, card2);

        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " reveals " + card1.getName() + " and " + card2.getName() + ".");

        // Determine opponent (in 2-player, the other player)
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();

        // Store the controller ID so the library reveal choice handler knows
        // to redirect the selected card to the controller's hand
        gameData.queueInteraction(new com.github.laxika.magicalvibes.model.PendingKarnScionRevealChoice(controllerId));

        // Present both cards to the opponent for selection
        Set<UUID> validIds = Set.of(card1.getId(), card2.getId());
        gameData.interaction.beginLibraryRevealChoice(opponentId, new ArrayList<>(revealedCards), validIds,
                false, true, false);

        gameBroadcastService.broadcastGameState(gameData);

        log.info("Game {} - {} reveals {} and {} for Karn Scion +1, opponent must choose",
                gameData.id, controllerName, card1.getName(), card2.getName());
    }
}
