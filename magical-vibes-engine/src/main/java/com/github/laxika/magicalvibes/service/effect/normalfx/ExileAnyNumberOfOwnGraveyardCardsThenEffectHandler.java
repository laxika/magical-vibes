package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfOwnGraveyardCardsThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an optional multi-card graveyard exile and its reflexive follow-up ability. */
@Component
@RequiredArgsConstructor
public class ExileAnyNumberOfOwnGraveyardCardsThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAnyNumberOfOwnGraveyardCardsThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileAnyNumberOfOwnGraveyardCardsThenEffect) effect;
        var state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();

        if (state.resolutionTimeExileAnyNumberThenEffectChoiceMade) {
            List<UUID> chosenCardIds = state.resolutionTimeExileAnyNumberThenEffectChosenCardIds;
            state.resolutionTimeExileAnyNumberThenEffectResume = false;
            state.resolutionTimeExileAnyNumberThenEffectChoiceMade = false;
            state.resolutionTimeExileAnyNumberThenEffectChosenCardIds = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            int exiledCount = exileChosenCards(gameData, entry, exileThen, chosenCardIds);
            entry.setEventValue(exiledCount);
            if (exiledCount > 0) {
                queueReflexiveAbility(gameData, entry, exileThen.thenEffect());
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileThen);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no matching cards in its controller's graveyard to exile."));
            return;
        }

        state.resolutionTimeExileAnyNumberThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, new ArrayList<>(candidates),
                candidates.size(), 0,
                entry.getCard().getName() + " — You may exile any number of matching cards from your graveyard.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
                                     ExileAnyNumberOfOwnGraveyardCardsThenEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.exileFilter(), sourceCardId))
                .toList();
    }

    private int exileChosenCards(GameData gameData, StackEntry entry,
                                 ExileAnyNumberOfOwnGraveyardCardsThenEffect effect,
                                 List<UUID> chosenCardIds) {
        if (chosenCardIds == null || chosenCardIds.isEmpty()) {
            return 0;
        }

        int exiledCount = 0;
        for (UUID cardId : chosenCardIds) {
            Card card = matchingCards(gameData, entry, effect).stream()
                    .filter(candidate -> candidate.getId().equals(cardId))
                    .findFirst()
                    .orElse(null);
            if (card == null) {
                continue;
            }
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
            exileService.exileCard(gameData, entry.getControllerId(), card);
            exiledCount++;
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getCard().getName() + " exiles ", card,
                            " from its controller's graveyard."));
        }
        return exiledCount;
    }

    private void queueReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        StackEntry reflexiveAbility = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(List.of(thenEffect)),
                entry.getSourcePermanentId(),
                List.of()
        );
        reflexiveAbility.setEventValue(entry.getEventValue());
        reflexiveAbility.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        gameData.stack.add(reflexiveAbility);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s reflexive ability triggers."));
    }
}
