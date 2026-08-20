package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardFromGraveyardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromGraveyardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileCardFromGraveyardThenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Integer> validIndices = matchingIndices(entry, exileThen, graveyard);

        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " has no " + exileThen.cardDescription() + " to exile from their graveyard."));
            return;
        }

        insertFollowUp(entry, effect, exileThen.thenEffect());
        if (validIndices.size() == 1) {
            Card card = graveyard.get(validIndices.getFirst());
            if (graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, card.getId(), card)) {
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(controllerId) + " exiles ", card,
                        " from their graveyard."));
            }
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        controllerId, validIndices, GraveyardChoiceDestination.EXILE,
                        "Choose a " + exileThen.cardDescription() + " to exile from your graveyard.")
                .mandatory(true)
                .build());
        log.info("Game {} - {} choosing a {} to exile from their graveyard",
                gameData.id, gameData.playerIdToName.get(controllerId), exileThen.cardDescription());
    }

    private List<Integer> matchingIndices(StackEntry entry,
                                           ExileCardFromGraveyardThenEffect effect, List<Card> graveyard) {
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    graveyard.get(i), effect.filter(), sourceCardId)) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private void insertFollowUp(StackEntry entry, CardEffect effect, CardEffect thenEffect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            CardEffect current = effects.get(i);
            if (current.equals(effect)
                    || current instanceof MayEffect may && may.wrapped().equals(effect)) {
                entry.insertEffectsToResolve(i + 1, List.of(thenEffect));
                return;
            }
        }
        throw new IllegalStateException("Could not locate exile-then effect on stack entry");
    }
}
