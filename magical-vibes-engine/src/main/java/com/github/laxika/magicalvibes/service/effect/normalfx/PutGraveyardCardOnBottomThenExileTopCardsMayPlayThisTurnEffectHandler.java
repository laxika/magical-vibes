package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the resolution-time graveyard choice and the following library exile.
 */
@Component
@RequiredArgsConstructor
public class PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileTopCardMayPlayThisTurnEffectHandler exileTopCardMayPlayThisTurnEffectHandler;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var putAndExileEffect = (PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffect) effect;
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();

        if (state.resolutionTimePutOnBottomThenExileTopCardsChoiceMade) {
            UUID chosenCardId = state.resolutionTimePutOnBottomThenExileTopCardsChosenCardId;
            state.resolutionTimePutOnBottomThenExileTopCardsChoiceMade = false;
            state.resolutionTimePutOnBottomThenExileTopCardsChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId == null) {
                return;
            }

            Card chosenCard = findMatchingCard(gameData, entry, putAndExileEffect, chosenCardId);
            if (chosenCard != null) {
                putOnBottomAndExileTopCards(gameData, entry, chosenCard, putAndExileEffect.exileCount());
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, putAndExileEffect);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " finds no matching card in its controller's graveyard to put on the bottom of its library."));
            return;
        }

        state.resolutionTimePutOnBottomThenExileTopCardsResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, new ArrayList<>(candidates), 1,
                entry.getCard().getName() + " — Put a matching card from your graveyard on the bottom of your library.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
            PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId))
                .toList();
    }

    private Card findMatchingCard(GameData gameData, StackEntry entry,
            PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffect effect, UUID cardId) {
        return matchingCards(gameData, entry, effect).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void putOnBottomAndExileTopCards(GameData gameData, StackEntry entry, Card card, int exileCount) {
        UUID controllerId = entry.getControllerId();
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        gameData.playerDecks.get(controllerId).addLast(card);
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " puts ", card, " on the bottom of its controller's library."));
        exileTopCardMayPlayThisTurnEffectHandler.resolve(gameData, entry,
                new ExileTopCardMayPlayThisTurnEffect(exileCount, false));
    }
}
