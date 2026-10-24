package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect} (Field of Ruin, Demolition Field):
 * destroys the target as the resolving step, then the players search in order (CR 608.2c), whether or not it was
 * destroyed. "Its controller" is the target's last controller.
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler implements NormalEffectHandlerBean,
        ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        stepDepartureSupport.destroyThenContinue(gameData, entry, effect, target.getId(), false, this);
    }

    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        List<DepartureResult> departures = entry.getResolutionResults().departures();
        UUID targetControllerId = departures.isEmpty() ? null : departures.getFirst().lastKnownControllerId();

        List<UUID> searchers = new ArrayList<>();
        if (((DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect) effect).targetControllerThenYou()) {
            // Sequential instructions follow the card's written order (CR 608.2c).
            if (targetControllerId != null) {
                searchers.add(targetControllerId);
            }
            if (entry.getControllerId() != null && !entry.getControllerId().equals(targetControllerId)) {
                searchers.add(entry.getControllerId());
            }
        } else {
            searchers.add(gameData.activePlayerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    searchers.add(playerId);
                }
            }
        }

        // Start the first player's search; the queue remainder rides the search interaction
        librarySearchSupport.startNextEachPlayerBasicLandSearch(gameData,
                LibrarySearchFollowUp.eachPlayerBasicLand(searchers, false));
    }
}
