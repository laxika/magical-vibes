package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingStudyCounterExileReturn;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnStudyCounterCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
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
public class ReturnStudyCounterCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameMutationCoordinator mutationCoordinator;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnStudyCounterCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> studyCards = gameData.getPlayerExiledCards(controllerId).stream()
                .filter(card -> gameData.exiledCardsWithStudyCounters.contains(card.getId()))
                .toList();
        if (studyCards.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + " has no exiled cards with study counters."));
            return;
        }

        gameData.queueInteraction(new PendingStudyCounterExileReturn());
        List<UUID> validIds = studyCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, new ArrayList<>(studyCards), validIds,
                false, true, false, false, false, 0, null, 1,
                "Choose a card with a study counter to return to your hand."));
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        log.info("Game {} - {} must choose from {} study counter cards", gameData.id,
                controllerName, studyCards.size());
    }
}
