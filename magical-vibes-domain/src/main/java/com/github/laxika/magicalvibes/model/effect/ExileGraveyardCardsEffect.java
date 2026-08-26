package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Unified graveyard-exile effect. The {@link GraveyardExileScope} selects which graveyard(s) are
 * affected and how targets (if any) are chosen; the {@code canTarget*} flags are derived from it.
 *
 * <p>Collapsed from {@code ExileCardsFromOwnGraveyardEffect} (OWN),
 * {@code ExileTargetCardFromGraveyardEffect} (TARGET_CARDS_ANY_GRAVEYARD),
 * {@code ExileTargetCardsFromOpponentGraveyardEffect} (TARGET_CARDS_OPPONENT_GRAVEYARD),
 * {@code ExileTargetPlayerGraveyardEffect} (TARGET_PLAYER_ENTIRE),
 * {@code ExileAllGraveyardsEffect} (ALL_PLAYERS) and
 * {@code ExileAllOpponentsGraveyardsEffect} (ALL_OPPONENTS).
 *
 * @param count            number of cards to exile (used by OWN and TARGET_CARDS_OPPONENT_GRAVEYARD;
 *                         1 for the single-target card scope, or {@code > 1} for
 *                         TARGET_CARDS_ANY_GRAVEYARD's "up to N target cards from a single
 *                         graveyard" flavour — Rag Dealer; unused for the whole-graveyard scopes).
 *                         Kept as an int — no live card uses a dynamic count here.
 * @param scope            which graveyard(s) are affected and how targets are chosen
 * @param filter           optional type restriction on the cards to exile (null = any card)
 * @param affectedPlayerId for OWN only: the player who must exile cards, baked in at trigger time
 *                         (e.g. the enchanted player for Curse of Oblivion); null = the controller
 * @param fromBattlefieldThisTurn whether matching cards must have been put into their graveyard
 *                                from the battlefield this turn
 * @param eventValueFilter       optional filter whose matching exiled targets are recorded on the
 *                                stack entry's event value
 * @param grantPlayPermissionUntilEndOfTurn whether exiled targeted controller-graveyard cards
 *                                           may be played by the controller this turn
 */
public record ExileGraveyardCardsEffect(
        int count, GraveyardExileScope scope, CardPredicate filter, UUID affectedPlayerId,
        boolean exactTargetCount, boolean trackWithSource, boolean fromBattlefieldThisTurn,
        CardPredicate eventValueFilter, boolean grantPlayPermissionUntilEndOfTurn) implements CardEffect {

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope, CardPredicate filter,
                                     UUID affectedPlayerId, boolean exactTargetCount,
                                     boolean trackWithSource, boolean fromBattlefieldThisTurn) {
        this(count, scope, filter, affectedPlayerId, exactTargetCount, trackWithSource,
                fromBattlefieldThisTurn, null, false);
    }

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope, CardPredicate filter,
                                     UUID affectedPlayerId, boolean exactTargetCount,
                                     boolean trackWithSource, boolean fromBattlefieldThisTurn,
                                     CardPredicate eventValueFilter) {
        this(count, scope, filter, affectedPlayerId, exactTargetCount, trackWithSource,
                fromBattlefieldThisTurn, eventValueFilter, false);
    }

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope) {
        this(count, scope, null, null, false, false, false, null, false);
    }

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope, CardPredicate filter) {
        this(count, scope, filter, null, false, false, false, null, false);
    }

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope,
                                     CardPredicate filter, UUID affectedPlayerId) {
        this(count, scope, filter, affectedPlayerId, false, false, false, null, false);
    }

    public ExileGraveyardCardsEffect(GraveyardExileScope scope) {
        this(0, scope, null, null, false, false, false, null, false);
    }

    public static ExileGraveyardCardsEffect targetedFromAnyGraveyardWithEventValue(
            int count, CardPredicate eventValueFilter) {
        return new ExileGraveyardCardsEffect(count, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                null, null, false, false, false, eventValueFilter, false);
    }

    public static ExileGraveyardCardsEffect exactTargetedFromAnyGraveyard(int count,
                                                                           CardPredicate filter,
                                                                           boolean trackWithSource) {
        return new ExileGraveyardCardsEffect(count, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                filter, null, true, trackWithSource, false, null, false);
    }

    public static ExileGraveyardCardsEffect ownAllMatchingUntilSourceLeaves(CardPredicate filter) {
        return new ExileGraveyardCardsEffect(0, GraveyardExileScope.OWN_ALL_MATCHING,
                filter, null, false, true, false, null, false);
    }

    public static ExileGraveyardCardsEffect allPlayersMatchingFromBattlefieldThisTurn(CardPredicate filter) {
        return new ExileGraveyardCardsEffect(0, GraveyardExileScope.ALL_PLAYERS,
                filter, null, false, false, true, null, false);
    }

    public static ExileGraveyardCardsEffect targetedFromControllerGraveyardMayPlayThisTurn() {
        return new ExileGraveyardCardsEffect(0, GraveyardExileScope.TARGET_CARDS_CONTROLLER_GRAVEYARD,
                null, null, false, false, false, null, true);
    }

    @Override
    public TargetSpec targetSpec() {
        // Per-scope spec reproducing the old conditional booleans exactly:
        //   TARGET_CARDS_ANY_GRAVEYARD      -> (graveyard=T, any=T)    -> ANY_GRAVEYARD_CARD
        //   TARGET_CARDS_OPPONENT_GRAVEYARD -> (graveyard=T, any=F)    -> GRAVEYARD_CARD
        //   TARGET_PLAYER_ENTIRE            -> (player=T)              -> PLAYER
        //   OWN / DYING_CREATURE_CONTROLLER /
        //   ALL_PLAYERS / ALL_OPPONENTS     -> no single validated target -> NONE
        return switch (scope) {
            case TARGET_CARDS_ANY_GRAVEYARD -> TargetSpec.benign(filter == null
                    ? TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)
                    : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.ALL_GRAVEYARDS));
            case TARGET_CARDS_OPPONENT_GRAVEYARD -> TargetSpec.benign(filter == null
                    ? TargetPredicates.graveyardCard(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                    : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.OPPONENT_GRAVEYARD));
            case TARGET_CARDS_CONTROLLER_GRAVEYARD -> TargetSpec.benign(filter == null
                    ? TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                    : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
            case TARGET_PLAYER_ENTIRE, TARGET_PLAYER_ALL_MATCHING -> TargetSpec.benign(TargetPredicates.player());
            default -> TargetSpec.NONE;
        };
    }
}
