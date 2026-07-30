package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameCreatureForEachControlledCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForSameNameCreatureForEachControlledCreatureEffect} (Doubling Chant):
 * collects the name of every creature the controller controls — one queue entry per creature, so two
 * copies of a name grant two searches — and begins the first optional same-name creature search. The
 * remaining names ride the search's {@link LibrarySearchFollowUp} and each subsequent pick is started
 * by {@code LibraryChoiceHandlerService} once the previous one resolves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForSameNameCreatureForEachControlledCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForSameNameCreatureForEachControlledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<String> names = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    names.add(permanent.getCard().getName());
                }
            }
        }

        if (names.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " controls no creatures."));
            log.info("Game {} - {} controls no creatures for Doubling Chant", gameData.id, playerName);
            return;
        }

        if (librarySearchSupport.startNextSameNamePick(gameData, controllerId,
                LibrarySearchFollowUp.sameNamePicks(names, true, LibrarySearchDestination.BATTLEFIELD))) {
            return;
        }

        if (!librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " finds no matching creature cards. Library is shuffled."));
        }
    }
}
