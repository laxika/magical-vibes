package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPsychoticEpisodeChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndTopCardChooseOneToBottomEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Psychotic Episode's combined revealed-hand and top-card choice. */
@Component
@RequiredArgsConstructor
public class RevealHandAndTopCardChooseOneToBottomEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandAndTopCardChooseOneToBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.pollPendingInteraction(PendingPsychoticEpisodeChoice.class) != null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        Card topCard = deck == null || deck.isEmpty() ? null : deck.getFirst();

        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        if (topCard != null) {
            String targetName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    targetName + " reveals ", topCard, " as the top card of their library."));
            cardRevealService.revealToAllPlayers(
                    gameData, targetPlayerId, GameEventFact.RevealZone.LIBRARY, List.of(topCard));
        }

        List<Card> revealedCards = new ArrayList<>(hand);
        if (topCard != null) {
            revealedCards.add(topCard);
        }
        if (revealedCards.isEmpty()) {
            return;
        }

        gameData.queueInteraction(new PendingPsychoticEpisodeChoice(
                targetPlayerId, topCard != null ? topCard.getId() : null));
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String prompt = controllerName + " chooses a card to put on the bottom of "
                + targetName + "'s library.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, revealedCards.stream().map(Card::getId).toList(),
                false, false, false, false, false, 0, null, 1, prompt, 1, false));
    }
}
