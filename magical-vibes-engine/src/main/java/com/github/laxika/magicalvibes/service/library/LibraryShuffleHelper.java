package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Centralized library shuffle utility that shuffles a player's library
 * and checks for ON_OPPONENT_SHUFFLES_LIBRARY triggers (e.g. Psychic Surgery).
 */
public final class LibraryShuffleHelper {

    private LibraryShuffleHelper() {}

    public static void shuffleLibrary(GameData gameData, UUID playerId) {
        gameData.libraryTopCardFreePlayPermissionsUntilEndOfTurn.remove(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);
        Collections.shuffle(deck);
        checkOpponentShuffleTriggers(gameData, playerId);
    }

    private static void checkOpponentShuffleTriggers(GameData gameData, UUID shufflingPlayerId) {
        gameData.forEachPermanent((controllerId, perm) -> {
            if (controllerId.equals(shufflingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_SHUFFLES_LIBRARY)) {
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            shufflingPlayerId,
                            perm.getId()
                    );
                    trigger.setNonTargeting(true);
                    trigger.setSourcePermanentSnapshot(new Permanent(perm));
                    gameData.enqueueTrigger(trigger);
            }
        });
    }
}
