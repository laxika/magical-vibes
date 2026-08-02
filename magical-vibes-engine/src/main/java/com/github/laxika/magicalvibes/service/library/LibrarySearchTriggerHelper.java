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

/**
 * Fires {@link EffectSlot#ON_OPPONENT_SEARCHES_LIBRARY} triggers (Ob Nixilis, Unshackled) from the
 * unified library-search choke point. The searching player is baked onto the triggered ability as
 * its {@code targetId}, so {@code TARGET_PLAYER}-scoped effects (sacrifice, life loss) act on them.
 */
public final class LibrarySearchTriggerHelper {

    private LibrarySearchTriggerHelper() {}

    public static void checkOpponentSearchTriggers(GameData gameData, GameLogService gameLogService,
                                                   UUID searchingPlayerId) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(searchingPlayerId)) return;

            for (var perm : List.copyOf(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY);
                if (effects.isEmpty()) continue;

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        searchingPlayerId,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        });
    }
}
