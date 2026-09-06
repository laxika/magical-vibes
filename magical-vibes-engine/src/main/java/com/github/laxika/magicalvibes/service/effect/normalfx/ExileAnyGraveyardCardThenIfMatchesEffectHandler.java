package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardThenIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an optional filtered exile from any graveyard and its conditional follow-up. */
@Component
@RequiredArgsConstructor
public class ExileAnyGraveyardCardThenIfMatchesEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAnyGraveyardCardThenIfMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileAnyGraveyardCardThenIfMatchesEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenCardId = gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade = false;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId == null) {
                insertAfterCurrentEffect(entry, effect, exileThen.noCardEffect());
                return;
            }

            Card chosen = findMatchingCard(gameData, entry, exileThen, chosenCardId);
            if (chosen == null || !graveyardReturnSupport.exileCardFromAnyGraveyard(
                    gameData, chosenCardId, chosen)) {
                insertAfterCurrentEffect(entry, effect, exileThen.noCardEffect());
                return;
            }

            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getCard().getName() + " exiles ", chosen,
                            " from a graveyard."));
            if (predicateEvaluationService.matchesCardPredicate(
                    chosen, exileThen.thenFilter(), entry.getCard().getId())) {
                entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex + 1,
                        List.of(exileThen.thenEffect()));
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileThen.exileFilter());
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no matching card in any graveyard to exile."));
            insertAfterCurrentEffect(entry, effect, exileThen.noCardEffect());
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, candidates, 1, 0,
                entry.getCard().getName() + " — You may exile a matching card from a graveyard.");
    }

    private List<Card> matchingCards(
            GameData gameData, StackEntry entry, CardPredicate filter) {
        List<Card> candidates = new ArrayList<>();
        UUID sourceCardId = entry.getCard().getId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(
                        card, filter, sourceCardId, gameData, playerId)) {
                    candidates.add(card);
                }
            }
        }
        return candidates;
    }

    private Card findMatchingCard(GameData gameData, StackEntry entry,
                                  ExileAnyGraveyardCardThenIfMatchesEffect effect, UUID cardId) {
        return matchingCards(gameData, entry, effect.exileFilter()).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void insertAfterCurrentEffect(StackEntry entry, CardEffect currentEffect,
                                          CardEffect followUp) {
        if (followUp == null) {
            return;
        }
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            CardEffect effect = effects.get(i);
            if (effect == currentEffect
                    || effect instanceof MayEffect may && may.wrapped().equals(currentEffect)) {
                entry.insertEffectsToResolve(i + 1, List.of(followUp));
                return;
            }
        }
        throw new IllegalStateException("Could not locate graveyard exile effect on stack entry");
    }
}
