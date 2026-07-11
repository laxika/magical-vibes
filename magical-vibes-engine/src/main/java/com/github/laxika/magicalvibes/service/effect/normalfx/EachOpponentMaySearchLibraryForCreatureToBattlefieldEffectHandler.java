package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect}: each opponent, in
 * APNAP order (active player first among opponents), may search their library for a creature card,
 * put it onto the battlefield, then shuffle. The per-player searches are driven through the shared
 * {@link LibrarySearchSupport}/{@code LibraryChoiceHandlerService} interaction pipeline; the APNAP
 * remainder rides the search interaction's follow-up. Used by Boldwyr Heavyweights.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySearchLibraryForCreatureToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Build APNAP-ordered queue of opponents only (skip the controller).
        List<UUID> searchers = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (!activePlayerId.equals(controllerId)) {
            searchers.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                searchers.add(playerId);
            }
        }

        librarySearchSupport.startNextEachPlayerCreatureToBattlefieldSearch(gameData,
                LibrarySearchFollowUp.eachPlayerCreatureToBattlefield(searchers));
    }
}
