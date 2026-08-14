package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
 * Resolves {@link RevealTopCardsAndSeparateEffect}: takes the top {@code count} cards of the
 * controller's library, then hands the pile split to the appropriate player. Reuses the shared card-pile
 * separation flow ({@link PendingPileSeparation} with {@link CardPileDisposition#HAND}); the
 * pile separator and chooser depend on the effect variant, while the chosen pile goes to hand
 * and the other to the graveyard. (Unesh, Criosphinx Sovereign; Steam Augury.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardsAndSeparateEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
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
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
            return;
        }

        if (e.disposition() == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " looks at the top " + revealedCards.size() + " cards of their library."));
        } else {
            String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
            gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames + "."));
        }

        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .toList();
        if (opponentIds.isEmpty()) {
            // No opponent to separate the piles — put the revealed cards into the controller's hand.
            for (Card card : revealedCards) {
                gameData.addCardToHand(controllerId, card);
            }
            return;
        }

        UUID opponentId = opponentIds.size() == 1 ? opponentIds.getFirst() : null;

        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), revealedCards, cardOwners, List.of(), List.of(), e.disposition(),
                !e.controllerSeparates()));

        if (e.disposition() == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE && opponentIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CuratorOpponentChoice());
            playerInputService.beginAnyTargetChoice(gameData, controllerId, List.of(), opponentIds,
                    "Choose an opponent to choose a pile for Curator of Destinies.");
            return;
        }

        UUID separatorId = e.controllerSeparates() ? controllerId : opponentId;
        String prompt = e.disposition() == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE
                ? "Look at the cards and select cards for the face-up pile (unselected cards form the face-down pile)."
                : "Separate the revealed cards into two piles. Select cards for Pile 1 (unselected form Pile 2).";
        playerInputService.beginMultiGraveyardChoice(gameData, separatorId, revealedCards, revealedCards.size(), prompt);
    }
}
