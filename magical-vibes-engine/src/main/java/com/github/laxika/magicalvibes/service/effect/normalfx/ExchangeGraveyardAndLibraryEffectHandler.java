package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeGraveyardAndLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Resolves {@link ExchangeGraveyardAndLibraryEffect}: both zones are emptied, then each one's cards go to the other
 * at once. Cards leaving the graveyard leave as one batch and cards entering it enter as one event; with two or
 * more entering, the owner orders them (CR 404.3).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeGraveyardAndLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeGraveyardAndLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExchangeGraveyardAndLibraryEffect exchange = (ExchangeGraveyardAndLibraryEffect) effect;
        UUID playerId = entry.getControllerId();

        List<Card> enteredGraveyard = List.of();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            List<Card> fromFirst = takeAll(gameData, playerId, exchange.first());
            List<Card> fromSecond = takeAll(gameData, playerId, exchange.second());
            List<Card> entered = new ArrayList<>();
            entered.addAll(putAll(gameData, playerId, exchange.second(), fromFirst, exchange.first()));
            entered.addAll(putAll(gameData, playerId, exchange.first(), fromSecond, exchange.second()));
            enteredGraveyard = entered;
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exchanges their " + zoneName(exchange.first())
                + " and " + zoneName(exchange.second()) + "."));
        log.info("Game {} - {} exchanges their {} and {}", gameData.id, playerName, exchange.first(), exchange.second());

        // CR 404.3: the owner arranges cards that enter their graveyard together in any order.
        if (enteredGraveyard.size() >= 2) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    playerId, List.copyOf(enteredGraveyard), false, playerId,
                    "Put the cards into your graveyard in any order.", 0, null, false, false,
                    Zone.GRAVEYARD, null));
        }
    }

    /** Empties the player's {@code zone} and returns the cards that move; a graveyard's tokens stay behind. */
    private List<Card> takeAll(GameData gameData, UUID playerId, Zone zone) {
        if (zone == Zone.GRAVEYARD) {
            return graveyardService.takeGraveyardCardsForZoneChange(gameData, playerId);
        }
        List<Card> cards = zone == Zone.HAND ? gameData.playerHands.get(playerId) : gameData.playerDecks.get(playerId);
        if (cards == null) {
            return List.of();
        }
        List<Card> taken = new ArrayList<>(cards);
        cards.clear();
        return taken;
    }

    /** Puts {@code cards} into the player's {@code zone}; returns the ones that entered a graveyard. */
    private List<Card> putAll(GameData gameData, UUID playerId, Zone zone, List<Card> cards, Zone from) {
        switch (zone) {
            case GRAVEYARD -> {
                if (from == Zone.LIBRARY) {
                    return graveyardService.addCardsFromLibraryToGraveyard(gameData, playerId, cards);
                }
                List<Card> entered = new ArrayList<>();
                gameData.rulesEvents.beginBatch();
                try {
                    for (Card card : cards) {
                        if (graveyardService.addCardToGraveyard(gameData, playerId, card, from)) {
                            entered.add(card);
                        }
                    }
                } finally {
                    gameData.rulesEvents.endBatch();
                }
                return entered;
            }
            case HAND -> gameData.playerHands.computeIfAbsent(playerId, ignored -> new ArrayList<>()).addAll(cards);
            default -> gameData.playerDecks.computeIfAbsent(playerId, ignored -> new ArrayList<>()).addAll(cards);
        }
        return List.of();
    }

    private static String zoneName(Zone zone) {
        return zone.name().toLowerCase(Locale.ROOT);
    }
}
