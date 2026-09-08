package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EachPlayerPutsCardFromHandOnTopOfLibraryState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPutsCardFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the APNAP hand choices for Sadistic Augermage. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerPutsCardFromHandOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPutsCardFromHandOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPutsCardFromHandOnTopOfLibraryState state =
                gameData.eachPlayerPutsCardFromHandOnTopOfLibrary;
        if (!state.active) {
            state.active = true;
            state.remaining.clear();
            if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
                state.remaining.addLast(gameData.activePlayerId);
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.addLast(playerId);
                }
            }
        }

        beginNextPlayer(gameData, entry);
    }

    private void beginNextPlayer(GameData gameData, StackEntry entry) {
        EachPlayerPutsCardFromHandOnTopOfLibraryState state =
                gameData.eachPlayerPutsCardFromHandOnTopOfLibrary;
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                continue;
            }

            List<Card> handSnapshot = List.copyOf(hand);
            List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                    .putRequiredCardOnLibrary(playerId, validCardIds, handSnapshot, HandToLibraryPlacement.TOP));
            log.info("Game {} - {} choosing a card from hand to put on top of their library for {}",
                    gameData.id, gameData.playerIdToName.get(playerId), entry.getCard().getName());
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
