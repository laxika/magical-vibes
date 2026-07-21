package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BrilliantUltimatumEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
 * Resolves {@link BrilliantUltimatumEffect}: exile the top {@code count} cards of the controller's
 * library, then hand the pile-separation choice to an opponent. Reuses the card-pile separation
 * flow ({@link PendingPileSeparation} with {@code CardPileDisposition.PLAY_FROM_EXILE}); the opponent's pile
 * selection and the controller's pile choice are completed by {@link BrilliantUltimatumSupport}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrilliantUltimatumEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BrilliantUltimatumEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BrilliantUltimatumEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        List<Card> exiledCards = new ArrayList<>();
        Map<UUID, UUID> cardOwners = new HashMap<>();
        for (int i = 0; i < e.count() && deck != null && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            exiledCards.add(card);
            cardOwners.put(card.getId(), controllerId);
        }

        if (exiledCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        String exiledNames = exiledCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + exiledNames + " (" + sourceName + ")."));

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            log.info("Game {} - {} has no opponent to separate the piles", gameData.id, sourceName);
            return;
        }

        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), exiledCards, cardOwners, List.of(), List.of(), CardPileDisposition.PLAY_FROM_EXILE));

        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, exiledCards, exiledCards.size(),
                "Separate the exiled cards into two piles. Select cards for Pile 1 (unselected form Pile 2).");
    }
}
