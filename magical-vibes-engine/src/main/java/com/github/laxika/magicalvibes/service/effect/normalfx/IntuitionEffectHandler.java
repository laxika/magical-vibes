package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.IntuitionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link IntuitionEffect}: the controller searches their library (as many cards as it holds if fewer) via a
 * {@link PendingInteraction.IntuitionSearchChoice} and reveals what they find, recording {@link StepKind#REVEAL} with
 * the found cards still in the library. A step that pauses at its end: the search records {@code REVEAL} as nothing
 * before it asks, and the answer ({@link #found}) records what was found before the resolution goes on. A library with
 * one card leaves no choice, so that card is found unasked; a prevented search, an empty library or a search with
 * nothing that can be found finds nothing. Shuffling is a later step.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntuitionEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntuitionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (IntuitionEffect) effect;
        UUID controllerId = entry.getControllerId();
        entry.recordStep(StepKind.REVEAL, StepOutcome.NONE);
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, false)) {
            return;
        }
        LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);

        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " searches their library but it is empty."));
            return;
        }

        UUID searchControllerId = librarySearchSupport.oppositionAgentControllerId(gameData, controllerId);
        List<Card> searchable = deck;
        int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
        if (topLimit < deck.size()) {
            searchable = deck.subList(0, topLimit);
        }
        List<Card> candidates = searchable.stream()
                .filter(card -> search.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, search.filter(), null, gameData, controllerId))
                .toList();
        if (candidates.isEmpty() || search.differentNames()
                && candidates.stream().map(Card::getName).distinct().count() < search.count()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " searches their library and finds "
                    + (search.differentNames() ? "no " + search.count() + " cards with different names." : "nothing.")));
            return;
        }
        if (candidates.size() == 1) {
            found(gameData, entry, controllerId, candidates, searchControllerId);
            return;
        }

        int count = Math.min(search.count(), candidates.size());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.IntuitionSearchChoice(
                controllerId, new ArrayList<>(candidates), count, search.differentNames(), searchControllerId));
        log.info("Game {} - {} searches their library for {} cards", gameData.id, controllerName, count);
    }

    /**
     * The cards the search of {@code controllerId}'s library found: revealed and recorded as the step's
     * {@link StepKind#REVEAL} result, still in the library; under Opposition Agent ({@code agentControllerId}) exiled
     * instead, playable by the Agent's controller, and nothing is revealed.
     */
    public void found(GameData gameData, StackEntry entry, UUID controllerId, List<Card> found,
                      UUID agentControllerId) {
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (found.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " searches their library and finds nothing."));
            return;
        }
        if (agentControllerId != null) {
            List<Card> deck = gameData.playerDecks.get(controllerId);
            for (Card card : found) {
                deck.remove(card);
                exileService.exileCard(gameData, controllerId, card);
                gameData.grantExilePlayPermission(card.getId(), agentControllerId, Duration.PERMANENT);
                gameData.updateExilePlayPermission(card.getId(), granted -> granted.withAnyManaType(true));
                gameLogService.append(gameData, GameLog.textCardText(
                        controllerName + " exiles ", card, " (Opposition Agent)."));
            }
            return;
        }
        GameLog.Builder reveal = GameLog.builder().text(controllerName + " reveals ");
        List<UUID> cardIds = new ArrayList<>();
        List<MovedCard> whereTheyAre = new ArrayList<>();
        for (int i = 0; i < found.size(); i++) {
            Card card = found.get(i);
            if (i > 0) {
                reveal.text(", ");
            }
            reveal.card(card);
            cardIds.add(card.getId());
            whereTheyAre.add(new MovedCard(card.getId(), card.getId(), Zone.LIBRARY, controllerId));
        }
        gameLogService.append(gameData, reveal.text(" from their library.").build());
        entry.recordStep(StepKind.REVEAL, StepOutcome.ofObjects(cardIds, List.of(controllerId), whereTheyAre));
    }
}
