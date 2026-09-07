package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DichotomancyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Dichotomancy's searches of the target opponent's library. */
@Component
@RequiredArgsConstructor
public class DichotomancyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DichotomancyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<String> names = gameData.playerBattlefields.getOrDefault(targetPlayerId, List.of()).stream()
                .filter(Permanent::isTapped)
                .filter(permanent -> !gameQueryService.isLand(gameData, permanent))
                .map(permanent -> gameQueryService.getEffectiveName(gameData, permanent))
                .filter(Objects::nonNull)
                .toList();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, targetPlayerId, true, controllerId)) {
            return;
        }

        LibrarySearchFollowUp followUp = LibrarySearchFollowUp.sameNamePicks(
                names, false, LibrarySearchDestination.BATTLEFIELD, targetPlayerId, controllerId, false);
        if (librarySearchSupport.startNextSameNamePick(gameData, controllerId, followUp)) {
            return;
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
    }
}
