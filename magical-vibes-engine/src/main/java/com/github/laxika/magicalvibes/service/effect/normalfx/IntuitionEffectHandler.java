package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntuitionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link IntuitionEffect}: the controller searches their library for three cards (as many
 * as the library holds if fewer) via a {@link PendingInteraction.IntuitionSearchChoice}. The
 * targeted opponent then picks one of the revealed cards; that card goes to the controller's hand
 * and the rest into their graveyard, then the library is shuffled.
 *
 * <p>A library of one card leaves the opponent no decision, so it is put into the controller's
 * hand here without prompting; an empty or unsearchable library just shuffles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntuitionEffectHandler implements NormalEffectHandlerBean {

    private static final int SEARCH_COUNT = 3;

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntuitionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;
        com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper
                .recordSearchAndQueueTriggers(gameData, gameLogService, controllerId);

        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        UUID searchControllerId = librarySearchSupport.oppositionAgentControllerId(gameData, controllerId);
        List<Card> searchable = deck;
        int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
        if (topLimit < deck.size()) {
            searchable = deck.subList(0, topLimit);
        }

        if (searchable.size() == 1) {
            Card onlyCard = searchable.getFirst();
            deck.remove(onlyCard);
            if (searchControllerId != null) {
                exileFoundCardForOppositionAgent(gameData, controllerId, onlyCard, searchControllerId);
            } else {
                gameData.addCardToHand(controllerId, onlyCard);
                gameLogService.append(gameData, GameLog.textCardText(
                        controllerName + " reveals ", onlyCard, " and puts it into their hand."));
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        int count = Math.min(SEARCH_COUNT, searchable.size());
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.IntuitionSearchChoice(
                        controllerId, entry.getTargetId(), new ArrayList<>(searchable), count, false,
                        searchControllerId));

        log.info("Game {} - {} searches library for {} cards for Intuition", gameData.id, controllerName, count);
    }

    /** Opposition Agent: a card found by the searching opponent is exiled and playable by the Agent's controller. */
    private void exileFoundCardForOppositionAgent(GameData gameData, UUID libraryOwnerId, Card card,
                                                 UUID agentControllerId) {
        exileService.exileCard(gameData, libraryOwnerId, card);
        gameData.exilePlayPermissions.put(card.getId(), agentControllerId);
        gameData.exilePlayAnyManaTypeWhileExiled.add(card.getId());
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(libraryOwnerId) + " exiles ", card, " (Opposition Agent)."));
    }
}
