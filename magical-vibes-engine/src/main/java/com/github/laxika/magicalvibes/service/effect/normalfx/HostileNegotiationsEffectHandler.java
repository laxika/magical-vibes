package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingHostileNegotiations;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HostileNegotiationsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Hostile Negotiations by creating two face-down piles and starting the controller choice. */
@Component
@RequiredArgsConstructor
public class HostileNegotiationsEffectHandler implements NormalEffectHandlerBean {

    private static final int PILE_SIZE = 3;

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HostileNegotiationsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        List<Card> pile1 = takeTopCards(gameData, controllerId);
        List<Card> pile2 = takeTopCards(gameData, controllerId);
        for (Card card : pile1) {
            gameData.addToExile(controllerId, card, null, true);
        }
        for (Card card : pile2) {
            gameData.addToExile(controllerId, card, null, true);
        }

        gameData.queueInteraction(new PendingHostileNegotiations(controllerId, opponentId, pile1, pile2));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " exiles two piles face down with Hostile Negotiations."));
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HostileNegotiationsFaceUpChoice(controllerId, pile1, pile2));
    }

    private List<Card> takeTopCards(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < PILE_SIZE && deck != null && !deck.isEmpty(); i++) {
            cards.add(deck.removeFirst());
        }
        return cards;
    }
}
