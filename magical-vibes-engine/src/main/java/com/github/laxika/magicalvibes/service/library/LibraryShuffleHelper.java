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
            List<CardEffect> effects = new ArrayList<>(
                    perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_SHUFFLES_LIBRARY));
            if (!controllerId.equals(shufflingPlayerId)) {
                effects.addAll(perm.getCard().getEffects(EffectSlot.ON_OPPONENT_SHUFFLES_LIBRARY));
            }
            if (causedByOwnSpellOrAbility) {
                effects.addAll(perm.getCard().getEffects(EffectSlot.ON_SPELL_OR_ABILITY_CAUSES_CONTROLLER_SHUFFLE));
            }
            for (CardEffect effect : effects) {
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
