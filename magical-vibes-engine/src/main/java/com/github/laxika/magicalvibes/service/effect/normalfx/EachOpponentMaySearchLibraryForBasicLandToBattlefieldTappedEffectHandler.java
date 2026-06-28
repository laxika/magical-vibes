package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect;
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
        gameData.pendingEachPlayerBasicLandSearchQueue.clear();
        gameData.pendingEachPlayerBasicLandSearchTapped = true;
        UUID activePlayerId = gameData.activePlayerId;
        // Add active player first if they are an opponent
        if (!activePlayerId.equals(controllerId)) {
            gameData.pendingEachPlayerBasicLandSearchQueue.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                gameData.pendingEachPlayerBasicLandSearchQueue.add(playerId);
            }
        }

        // Start the first opponent's search
        librarySearchSupport.startNextEachPlayerBasicLandSearch(gameData);
    }
}
