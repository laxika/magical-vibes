package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerMaySearchLibraryForBasicLandToBattlefieldEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the basic-land search for each selected target player in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachTargetPlayerMaySearchLibraryForBasicLandToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetPlayerMaySearchLibraryForBasicLandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> remainingTargets = new ArrayList<>(entry.getTargetIds());
        List<UUID> searchers = new ArrayList<>(remainingTargets.size());

        for (UUID playerId : basicLandSearchQueueSupport.apnapOrder(gameData)) {
            if (remainingTargets.remove(playerId)) {
                searchers.add(playerId);
            }
        }
        searchers.addAll(remainingTargets);

        librarySearchSupport.startNextEachPlayerBasicLandSearch(
                gameData, LibrarySearchFollowUp.eachPlayerBasicLand(searchers, false));
    }
}
