package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import com.github.laxika.magicalvibes.service.trigger.LibraryAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.LibraryWatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Centralized library shuffle utility that shuffles a player's library
 * and queues opponent-only and any-player shuffle triggers.
 */
public final class LibraryShuffleHelper {

    private LibraryShuffleHelper() {}

    public static void shuffleLibrary(GameData gameData, UUID playerId) {
        gameData.restoreBombardmentLibraryCards(playerId);
        gameData.libraryTopCardFreePlayPermissionsUntilEndOfTurn.remove(playerId);
        gameData.pendingCommanderZoneMoves.replaceAll(move -> move.ownerId().equals(playerId)
                && move.destination() == com.github.laxika.magicalvibes.model.Zone.LIBRARY ? move.shuffled() : move);
        List<Card> deck = gameData.playerDecks.get(playerId);
        Collections.shuffle(deck);
        checkShuffleTriggers(gameData, playerId);
    }

    /**
     * Shuffles the library recorded in {@code libraryToShuffleAfterFreeCast}, if any. Called once the
     * free-cast spell has been put on the stack, so shuffle triggers go on the stack above it.
     */
    public static void shuffleLibraryPendingAfterFreeCast(GameData gameData) {
        UUID playerId = gameData.libraryToShuffleAfterFreeCast;
        if (playerId == null) {
            return;
        }
        gameData.libraryToShuffleAfterFreeCast = null;
        shuffleLibrary(gameData, playerId);
    }

    private static void checkShuffleTriggers(GameData gameData, UUID shufflingPlayerId) {
        UUID causingControllerId = gameData.currentlyResolvingControllerId != null
                ? gameData.currentlyResolvingControllerId
                : gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry.getControllerId()
                : null;
        boolean causedByOwnSpellOrAbility = shufflingPlayerId.equals(causingControllerId);
        gameData.forEachPermanent((controllerId, perm) -> {
            // Each ability is one object of all its effects (CR 603.3).
            List<List<CardEffect>> abilities = new ArrayList<>(LibraryAbilityReader.printedEffectsByAbility(
                    perm.getCard(), LibraryWatch.ON_ANY_PLAYER_SHUFFLES_LIBRARY));
            if (!controllerId.equals(shufflingPlayerId)) {
                abilities.addAll(LibraryAbilityReader.printedEffectsByAbility(perm.getCard(),
                        LibraryWatch.ON_OPPONENT_SHUFFLES_LIBRARY));
            }
            if (causedByOwnSpellOrAbility) {
                abilities.addAll(LibraryAbilityReader.printedEffectsByAbility(perm.getCard(),
                        LibraryWatch.ON_SPELL_OR_ABILITY_CAUSES_CONTROLLER_SHUFFLE));
            }
            for (List<CardEffect> effects : abilities) {
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
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
