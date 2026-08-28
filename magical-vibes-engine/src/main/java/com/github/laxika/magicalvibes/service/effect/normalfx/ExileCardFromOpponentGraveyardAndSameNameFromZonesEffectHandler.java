package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.DeadlyCoverUpContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromOpponentGraveyardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Deadly Cover-Up's non-targeting graveyard exile and same-name search. */
@Component
@RequiredArgsConstructor
public class ExileCardFromOpponentGraveyardAndSameNameFromZonesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromOpponentGraveyardAndSameNameFromZonesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DeadlyCoverUpContext context = gameData.graveyardTargetOperation.resolutionTimeDeadlyCoverUp;
        if (context != null && context.chosenCardId() != null) {
            gameData.graveyardTargetOperation.resolutionTimeDeadlyCoverUp = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            resolveChosenCard(gameData, entry, context.chosenCardId());
            return;
        }

        List<Card> opponentGraveyardCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(entry.getControllerId())) {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                if (graveyard != null) {
                    opponentGraveyardCards.addAll(graveyard);
                }
            }
        }

        if (opponentGraveyardCards.isEmpty()) {
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeDeadlyCoverUp = new DeadlyCoverUpContext(null);
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                opponentGraveyardCards, 1, 1,
                entry.getCard().getName() + " — Choose a card from an opponent's graveyard to exile.");
    }

    private void resolveChosenCard(GameData gameData, StackEntry entry, UUID cardId) {
        Card chosenCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (chosenCard == null || ownerId == null || ownerId.equals(entry.getControllerId())) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
        exileService.exileCard(gameData, ownerId, chosenCard);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, ownerId, List.of(chosenCard));
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " exiles ", chosenCard, " from an opponent's graveyard."));

        String cardName = chosenCard.getName();
        List<Card> matchingCards = collectMatchingCards(gameData, ownerId, cardName);
        List<Card> library = gameData.playerDecks.get(ownerId);
        if (matchingCards.isEmpty()) {
            if (library != null) {
                Collections.shuffle(library);
            }
            return;
        }

        playerInputService.beginMultiZoneExileChoice(gameData, entry.getControllerId(), matchingCards,
                matchingCards.size(), ownerId, cardName, true);
    }

    private List<Card> collectMatchingCards(GameData gameData, UUID playerId, String cardName) {
        List<Card> matchingCards = new ArrayList<>();
        addMatchingCards(matchingCards, gameData.playerGraveyards.get(playerId), cardName);
        addMatchingCards(matchingCards, gameData.playerHands.get(playerId), cardName);
        addMatchingCards(matchingCards, gameData.playerDecks.get(playerId), cardName);
        return matchingCards;
    }

    private void addMatchingCards(List<Card> matchingCards, List<Card> cards, String cardName) {
        if (cards != null) {
            matchingCards.addAll(cards.stream().filter(card -> card.getName().equals(cardName)).toList());
        }
    }
}
