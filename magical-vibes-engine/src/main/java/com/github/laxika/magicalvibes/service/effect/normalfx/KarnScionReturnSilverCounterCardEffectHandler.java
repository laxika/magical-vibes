package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KarnScionReturnSilverCounterCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KarnScionReturnSilverCounterCardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KarnScionReturnSilverCounterCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Find all exiled cards owned by the controller with silver counters
        List<Card> exiledCards = gameData.getPlayerExiledCards(controllerId);
        List<Card> silverCards = exiledCards.stream()
                .filter(c -> gameData.exiledCardsWithSilverCounters.contains(c.getId()))
                .toList();

        if (silverCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " has no exiled cards with silver counters.");
            log.info("Game {} - {} has no silver counter cards for Karn Scion -1", gameData.id, controllerName);
            return;
        }

        if (silverCards.size() == 1) {
            // Only one card — put it directly in hand
            Card card = silverCards.getFirst();
            gameData.removeFromExile(card.getId());
            gameData.exiledCardsWithSilverCounters.remove(card.getId());
            gameData.addCardToHand(controllerId, card);

            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " returns " + card.getName() + " from exile to their hand.");
            log.info("Game {} - {} returns {} from exile (silver counter) to hand",
                    gameData.id, controllerName, card.getName());
            return;
        }

        // Multiple cards — let the controller choose
        gameData.queueInteraction(new com.github.laxika.magicalvibes.model.PendingKarnScionExileReturn());
        List<UUID> validIds = silverCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice(
                controllerId, new ArrayList<>(silverCards), validIds,
                false, true, false, false, 0, null, 1,
                "Choose a card with a silver counter to return to your hand."));

        gameBroadcastService.broadcastGameState(gameData);

        log.info("Game {} - {} must choose from {} silver counter cards for Karn Scion -1",
                gameData.id, controllerName, silverCards.size());
    }
}
