package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fires library-search triggers from the unified library-search choke point. */
public final class LibrarySearchTriggerHelper {

    private LibrarySearchTriggerHelper() {}

    /**
     * Records that a player searched their library and queues every "whenever ... searches a library"
     * trigger it causes. Call this once per search, including searches that find nothing, since
     * those still count as searching.
     * <p>
     * Both trigger kinds are covered: permanents controlled by the searching
     * player fire their {@link EffectSlot#ON_CONTROLLER_SEARCHES_LIBRARY} effects, and permanents
     * controlled by every other player fire their {@link EffectSlot#ON_OPPONENT_SEARCHES_LIBRARY}
     * effects with the searching player as the trigger's target. Triggers are only enqueued (and
     * logged) here; they go on the stack the next time pending triggers are processed.
     *
     * @param gameData          the game being mutated
     * @param gameLogService    used to log each ability that triggers
     * @param searchingPlayerId the player whose library was searched; also added to
     *                          {@code playersWhoSearchedLibraryThisTurn}
     */
    public static void recordSearchAndQueueTriggers(GameData gameData, GameLogService gameLogService,
                                                    UUID searchingPlayerId) {
        gameData.playersWhoSearchedLibraryThisTurn.add(searchingPlayerId);
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (var perm : List.copyOf(battlefield)) {
                boolean controllerSearch = controllerId.equals(searchingPlayerId);
                List<CardEffect> effects = perm.getCard().getEffects(controllerSearch
                        ? EffectSlot.ON_CONTROLLER_SEARCHES_LIBRARY
                        : EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY);
                if (effects.isEmpty()) continue;

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        controllerSearch ? null : searchingPlayerId,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        });
    }
}
