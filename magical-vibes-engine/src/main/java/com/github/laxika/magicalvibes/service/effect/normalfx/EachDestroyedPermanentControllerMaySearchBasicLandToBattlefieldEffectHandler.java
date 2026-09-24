package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerMaySearchBasicLandToBattlefieldEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the per-destroyed-permanent optional basic-land search rider. */
@Component
@RequiredArgsConstructor
public class EachDestroyedPermanentControllerMaySearchBasicLandToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachDestroyedPermanentControllerMaySearchBasicLandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<LibrarySearchFollowUp.BasicLandsPick> picks = new ArrayList<>();
        for (UUID playerId : basicLandSearchQueueSupport.apnapOrder(gameData)) {
            long destroyedCount = entry.getEventPlayerIds().stream()
                    .filter(playerId::equals)
                    .count();
            for (int i = 0; i < destroyedCount; i++) {
                picks.add(new LibrarySearchFollowUp.BasicLandsPick(playerId, 1, false));
            }
        }

        if (!picks.isEmpty()) {
            basicLandSearchQueueSupport.advance(gameData,
                    LibrarySearchFollowUp.basicLandSearches(picks, List.of(), true));
        }
    }
}
