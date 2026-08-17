package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;
import java.util.UUID;

/**
 * Queued entry for sequential graveyard-return prompts when a player must choose cards
 * one at a time (e.g. Fall of the Thran chapters II/III, Grim Captain's Call).
 *
 * @param playerId                the player who makes the choice
 * @param remainingCount          how many more picks of the same filter remain after this one
 * @param filter                  predicate restricting which graveyard cards qualify
 * @param destination             where the returned card goes — {@code HAND} or {@code BATTLEFIELD}
 * @param skipRemainingOnDecline  if {@code true}, declining removes all remaining entries for this
 *                                player (used by "each player returns up to N" effects); if
 *                                {@code false}, only the current entry is skipped (used when each
 *                                entry is an independent choice, e.g. Grim Captain's Call)
 * @param mandatory                if {@code true}, the player must choose a card when one is
 *                                available
 * @param fromBattlefieldThisTurn if {@code true}, only cards tracked as having entered the graveyard
 *                                from the battlefield this turn are eligible
 * @param distinctManaValues      if {@code true}, each selected card must have a mana value not
 *                                already selected in this queued return flow
 * @param excludedManaValues      mana values already selected in this queued return flow
 */
public record PendingGraveyardReturnChoice(UUID playerId, int remainingCount, CardPredicate filter,
                                           GraveyardChoiceDestination destination,
                                           boolean skipRemainingOnDecline,
                                           boolean mandatory, boolean fromBattlefieldThisTurn,
                                           boolean distinctManaValues, Set<Integer> excludedManaValues) {

    public PendingGraveyardReturnChoice {
        excludedManaValues = excludedManaValues == null ? Set.of() : Set.copyOf(excludedManaValues);
    }

    public PendingGraveyardReturnChoice(UUID playerId, int remainingCount, CardPredicate filter,
                                        GraveyardChoiceDestination destination,
                                        boolean skipRemainingOnDecline) {
        this(playerId, remainingCount, filter, destination, skipRemainingOnDecline, false, false,
                false, Set.of());
    }

    public PendingGraveyardReturnChoice(UUID playerId, int remainingCount, CardPredicate filter,
                                        GraveyardChoiceDestination destination,
                                        boolean skipRemainingOnDecline, boolean mandatory,
                                        boolean fromBattlefieldThisTurn) {
        this(playerId, remainingCount, filter, destination, skipRemainingOnDecline, mandatory,
                fromBattlefieldThisTurn, false, Set.of());
    }
}
