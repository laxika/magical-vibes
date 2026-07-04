package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry);
    }

    private void doResolve(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Build APNAP-ordered queue of opponents only (skip the controller)
        List<UUID> searchers = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        // Add active player first if they are an opponent
        if (!activePlayerId.equals(controllerId)) {
            searchers.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                searchers.add(playerId);
            }
        }

        // Start the first opponent's search; the queue remainder rides the search interaction
        librarySearchSupport.startNextEachPlayerBasicLandSearch(gameData,
                LibrarySearchFollowUp.eachPlayerBasicLand(searchers, true));
    }
}
