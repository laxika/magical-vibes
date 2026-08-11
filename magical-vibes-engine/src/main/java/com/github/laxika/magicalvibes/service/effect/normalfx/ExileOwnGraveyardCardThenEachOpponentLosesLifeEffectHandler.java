package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves an optional, non-targeting filtered graveyard exile and its conditional life-loss rider.
 */
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardCardThenEachOpponentLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect) effect;
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();

        if (state.resolutionTimeExileThenEachOpponentLosesLifeChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileThenEachOpponentLosesLifeChosenCardId;
            state.resolutionTimeExileThenEachOpponentLosesLifeChoiceMade = false;
            state.resolutionTimeExileThenEachOpponentLosesLifeChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId == null) {
                return;
            }

            Card chosen = findMatchingCard(gameData, entry, exileEffect, chosenCardId);
            if (chosen != null) {
                exileAndApplyLifeLoss(gameData, entry, chosen, exileEffect.lifeLoss());
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileEffect);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no matching card in its controller's graveyard to exile."));
            return;
        }

        if (candidates.size() == 1) {
            exileAndApplyLifeLoss(gameData, entry, candidates.getFirst(), exileEffect.lifeLoss());
            return;
        }

        state.resolutionTimeExileThenEachOpponentLosesLifeResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, new ArrayList<>(candidates), 1,
                entry.getCard().getName() + " — You may exile a matching card from your graveyard.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
            ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect effect) {
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
            ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect effect, UUID cardId) {
        return matchingCards(gameData, entry, effect).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void exileAndApplyLifeLoss(GameData gameData, StackEntry entry, Card card, int lifeLoss) {
        UUID controllerId = entry.getControllerId();
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        exileService.exileCard(gameData, controllerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " exiles ", card, " from its controller's graveyard."));

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        }
    }
}
