package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EmergentUltimatumEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Emergent Ultimatum's restricted library search. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmergentUltimatumEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EmergentUltimatumEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(controllerId);
        LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);

        int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
        List<Card> searchableCards = library == null
                ? List.of()
                : library.subList(0, Math.min(topLimit, library.size()));
        List<Card> pool = searchableCards.stream()
                .filter(card -> card.getColors().size() == 1)
                .toList();

        if (pool.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " finds no monocolored cards for Emergent Ultimatum."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.EmergentUltimatumSearchChoice(
                        controllerId, new ArrayList<>(pool)));
        log.info("Game {} - Awaiting {} to choose Emergent Ultimatum cards from a pool of {}",
                gameData.id, gameData.playerIdToName.get(controllerId), pool.size());
    }
}
