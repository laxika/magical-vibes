package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealTopCardsAndSeparateEffect}: reveal the top {@code count} cards of the
 * controller's library, then hand the pile split to an opponent. Reuses the shared card-pile
 * separation flow ({@link PendingPileSeparation} with {@link CardPileDisposition#HAND}); the
 * opponent's pile selection and the controller's pile choice (chosen pile → hand, other →
 * graveyard) are completed by {@code GraveyardReturnSupport}. (Unesh, Criosphinx Sovereign.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardsAndSeparateEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsAndSeparateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTopCardsAndSeparateEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        List<Card> revealedCards = new ArrayList<>();
        Map<UUID, UUID> cardOwners = new HashMap<>();
        for (int i = 0; i < e.count() && deck != null && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            cardOwners.put(card.getId(), controllerId);
        }

        if (revealedCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty."));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + revealedNames + "."));

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            // No opponent to separate the piles — put the revealed cards into the controller's hand.
            for (Card card : revealedCards) {
                gameData.addCardToHand(controllerId, card);
            }
            return;
        }

        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), revealedCards, cardOwners, List.of(), List.of(), CardPileDisposition.HAND));

        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, revealedCards, revealedCards.size(),
                "Separate the revealed cards into two piles. Select cards for Pile 1 (unselected form Pile 2).");
    }
}
