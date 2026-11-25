package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameAsEachPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForSameNameAsEachPermanentEffect}: reads the name of each permanent its subject names
 * (none for a face-down one, CR 708.2a) and begins the one search, whose picks {@code LibraryChoiceHandlerService}
 * takes in turn before the found cards enter together and the library is shuffled. Pauses at the end of its step:
 * every pick records the card it found as the SEARCH step's outcome.
 */
@Component
@RequiredArgsConstructor
public class SearchLibraryForSameNameAsEachPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForSameNameAsEachPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (SearchLibraryForSameNameAsEachPermanentEffect) effect;
        entry.recordStep(StepKind.SEARCH, StepOutcome.NONE);
        UUID searcherId = entry.getControllerId();
        UUID ownerId = subjectResolutionService.player(gameData, entry, effect, search.libraryOwner());
        if (ownerId == null) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, search.permanents())) {
            String name = gameQueryService.getEffectiveName(gameData, permanent);
            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        }
        // "You may search ..., then shuffle": a player who can't search can't choose to, so nothing is shuffled
        // (Leonin Arbiter's ruling); "search ..., then shuffle" still shuffles.
        if (librarySearchSupport.isSearchPrevented(gameData, searcherId, ownerId, !search.optional(), searcherId)) {
            return;
        }
        boolean yourLibrary = ownerId.equals(searcherId);
        LibrarySearchDestination destination = search.tapped()
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED : LibrarySearchDestination.BATTLEFIELD;
        LibrarySearchFollowUp followUp = LibrarySearchFollowUp.NONE.withRemainingSameNamePicks(
                new LibrarySearchFollowUp.SameNamePickQueue(names, search.creatureOnly(), destination,
                        yourLibrary ? null : ownerId, searcherId, search.optional(), true, List.of()));
        if (librarySearchSupport.startNextSameNamePick(gameData, searcherId, followUp)) {
            return;
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        String searcherName = gameData.playerIdToName.get(searcherId);
        gameLogService.append(gameData, GameLog.text(yourLibrary
                ? searcherName + " finds no matching cards. Library is shuffled."
                : searcherName + " finds no matching cards. " + gameData.playerIdToName.get(ownerId)
                        + "'s library is shuffled."));
    }
}
