package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link EachPlayerMaySearchLibraryForCardsToHandEffect}: each player, in APNAP
 * order (active player first), may search their library for up to {@code count} creature cards,
 * reveal them, put them into their hand, then shuffle. The per-player searches are driven through
 * the shared {@link LibrarySearchSupport}/{@code LibraryChoiceHandlerService} interaction pipeline;
 * the APNAP remainder rides the search interaction's follow-up. Used by Weird Harvest.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMaySearchLibraryForCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMaySearchLibraryForCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMaySearchLibraryForCardsToHandEffect search = (EachPlayerMaySearchLibraryForCardsToHandEffect) effect;
        int count = amountEvaluationService.evaluate(gameData, search.count(),
                AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }

        // APNAP-ordered queue: active player first, then the others in turn order.
        List<UUID> searchers = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        searchers.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                searchers.add(playerId);
            }
        }

        librarySearchSupport.startNextEachPlayerToHandSearch(gameData,
                LibrarySearchFollowUp.eachPlayerCardsToHand(searchers, count, search.creatureOnly()));
    }
}
