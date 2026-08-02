package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntuitionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntuitionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        if (deck.size() == 1) {
            Card onlyCard = deck.removeFirst();
            gameData.addCardToHand(controllerId, onlyCard);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    controllerName + " reveals ", onlyCard, " and puts it into their hand."));
            return;
        }

        int count = Math.min(SEARCH_COUNT, deck.size());
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.IntuitionSearchChoice(
                        controllerId, entry.getTargetId(), new ArrayList<>(deck), count));

        log.info("Game {} - {} searches library for {} cards for Intuition", gameData.id, controllerName, count);
    }
}
