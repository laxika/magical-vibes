package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BurningRuneDemonEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Burning-Rune Demon's two-card opponent-choice library search. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BurningRuneDemonEffectHandler implements NormalEffectHandlerBean {

    private static final int SEARCH_COUNT = 2;

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BurningRuneDemonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        BurningRuneDemonEffect burningRuneDemonEffect = (BurningRuneDemonEffect) effect;
        List<Card> candidates = deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, burningRuneDemonEffect.filter(), null, gameData, controllerId))
                .toList();
        if (distinctNameCount(candidates) < SEARCH_COUNT) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " cannot find two cards with different names. Library is shuffled."));
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.IntuitionSearchChoice(
                controllerId, opponentId, new ArrayList<>(candidates), SEARCH_COUNT, true));
        log.info("Game {} - {} searches library for two differently named cards with Burning-Rune Demon",
                gameData.id, controllerName);
    }

    private int distinctNameCount(List<Card> cards) {
        return (int) cards.stream().map(Card::getName).distinct().count();
    }
}
