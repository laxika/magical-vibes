package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resolves the resolution-time graveyard choice and the shared-creature-type counter rider.
 */
@Component
@RequiredArgsConstructor
public class ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect) effect;
        var state = gameData.graveyardTargetOperation;

        if (state.resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChosenCardId;
            state.resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChoiceMade = false;
            state.resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId != null) {
                Card chosenCard = gameQueryService.findCardInGraveyardById(gameData, chosenCardId);
                if (chosenCard != null) {
                    exileAndPutCounters(gameData, entry, exileEffect, chosenCard);
                }
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
            exileAndPutCounters(gameData, entry, exileEffect, candidates.getFirst());
            return;
        }

        state.resolutionTimeExileThenPutCountersOnSharedTypeCreaturesResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                new ArrayList<>(candidates),
                1,
                1,
                "Choose a " + exileEffect.cardDescription() + " to exile from your graveyard.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
                                      ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> !Objects.equals(card.getId(), sourceCardId))
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), sourceCardId, gameData, entry.getControllerId()))
                .toList();
    }

    private void exileAndPutCounters(GameData gameData, StackEntry entry,
                                     ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect effect,
                                     Card card) {
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, card.getId(), card)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(gameData.playerIdToName.get(entry.getControllerId()) + " exiles ",
                        card, " from their graveyard."));

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        Permanent exiledCardAsPermanent = new Permanent(card);
        List<Permanent> matchingCreatures = battlefield.stream()
                .filter(permanent -> !isSource(entry, permanent))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> gameQueryService.shareCreatureType(
                        gameData, exiledCardAsPermanent, permanent))
                .toList();

        for (Permanent permanent : matchingCreatures) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, permanent, effect.counterType(), effect.counterCount());
        }
    }

    private boolean isSource(StackEntry entry, Permanent permanent) {
        return Objects.equals(entry.getSourcePermanentId(), permanent.getId())
                || Objects.equals(entry.getCard().getId(), permanent.getCard().getId());
    }
}
