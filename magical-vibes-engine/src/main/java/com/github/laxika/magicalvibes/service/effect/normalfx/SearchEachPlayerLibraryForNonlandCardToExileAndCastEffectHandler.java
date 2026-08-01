package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingEachPlayerLibraryExile;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect}: the controller
 * searches every player's library in APNAP order (their own first when they are the active player),
 * exiling one nonland card from each and shuffling that library, then may cast the exiled cards
 * without paying their mana costs. The per-player searches ride the shared library-search
 * interaction pipeline with {@link PendingEachPlayerLibraryExile} carrying the remainder; the free
 * casts reuse the shared exiled-spell cast queue. (Jace, Architect of Thought's −8.)
 */
@Component
@RequiredArgsConstructor
public class SearchEachPlayerLibraryForNonlandCardToExileAndCastEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> searchOrder = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (gameData.orderedPlayerIds.contains(activePlayerId)) {
            searchOrder.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                searchOrder.add(playerId);
            }
        }

        gameData.queueInteraction(
                new PendingEachPlayerLibraryExile(entry.getControllerId(), searchOrder, List.of()));
        librarySearchSupport.advanceEachPlayerNonlandExile(gameData);
    }
}
