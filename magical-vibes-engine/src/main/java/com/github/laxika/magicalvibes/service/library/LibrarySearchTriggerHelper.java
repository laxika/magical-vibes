package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.LibraryEvent;
import com.github.laxika.magicalvibes.service.trigger.LibraryAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.LibraryWatch;

import java.util.UUID;

/** Records library searches at the unified library-search choke point. */
public final class LibrarySearchTriggerHelper {

    private LibrarySearchTriggerHelper() {}

    /**
     * Records that a player searched their library, and emits the search as a rules event, which the "whenever ...
     * searches a library" abilities trigger on (CR 603.2): the trigger pipeline hands them to the stack door
     * ({@code TriggerCollectionService.checkLibrarySearchTriggers}) as the event's batch closes. Call this once per
     * search, including searches that find nothing, since those still count as searching.
     *
     * @param gameData          the game being mutated
     * @param searchingPlayerId the player whose library was searched; also added to
     *                          {@code playersWhoSearchedLibraryThisTurn}
     */
    public static void recordSearchAndQueueTriggers(GameData gameData, UUID searchingPlayerId) {
        gameData.playersWhoSearchedLibraryThisTurn.add(searchingPlayerId);
        gameData.rulesEvents.emit(LibraryEvent.searched(searchingPlayerId));
    }

    /**
     * Whether any permanent on the battlefield cares about library searches at all. An optional
     * search of an empty library is only worth offering when this is true, since nothing else about
     * the search is observable.
     */
    public static boolean anyPermanentReactsToLibrarySearches(GameData gameData) {
        boolean[] found = {false};
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (var perm : battlefield) {
                if (!LibraryAbilityReader.printed(perm.getCard(), LibraryWatch.ON_CONTROLLER_SEARCHES_LIBRARY).isEmpty()
                        || !LibraryAbilityReader.printed(perm.getCard(), LibraryWatch.ON_OPPONENT_SEARCHES_LIBRARY)
                        .isEmpty()) {
                    found[0] = true;
                }
            }
        });
        return found[0];
    }
}
