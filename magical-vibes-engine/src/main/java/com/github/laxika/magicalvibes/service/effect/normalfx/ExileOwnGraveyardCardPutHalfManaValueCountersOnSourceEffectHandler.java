package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Torrent Sculptor's non-targeting graveyard choice. */
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect) effect;
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;

        if (state.resolutionTimeExileOwnGraveyardCardPutCountersChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileOwnGraveyardCardPutCountersChosenCardId;
            state.resolutionTimeExileOwnGraveyardCardPutCountersChoiceMade = false;
            state.resolutionTimeExileOwnGraveyardCardPutCountersChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId == null) {
                return;
            }

            Card chosen = findMatchingCard(gameData, entry, exileEffect, chosenCardId);
            if (chosen != null) {
                exileAndApplyCounters(gameData, entry, chosen);
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
            exileAndApplyCounters(gameData, entry, candidates.getFirst());
            return;
        }

        state.resolutionTimeExileOwnGraveyardCardPutCountersResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                new ArrayList<>(candidates), 1,
                entry.getCard().getName() + " - Choose an instant or sorcery card to exile from your graveyard.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
                                     ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect effect) {
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
                                  ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect effect,
                                  UUID cardId) {
        return matchingCards(gameData, entry, effect).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void exileAndApplyCounters(GameData gameData, StackEntry entry, Card card) {
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        exileService.exileCard(gameData, entry.getControllerId(), card);
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " exiles ", card, " from its controller's graveyard."));

        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, source,
                    (card.getManaValue() + 1) / 2);
        }
    }
}
