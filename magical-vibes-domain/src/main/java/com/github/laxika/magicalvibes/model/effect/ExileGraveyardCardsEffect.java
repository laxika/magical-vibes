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
 * @param filter           for TARGET_CARDS_ANY_GRAVEYARD only: an optional type restriction on the
 *                         targeted card (null = any card)
 * @param affectedPlayerId for OWN only: the player who must exile cards, baked in at trigger time
 *                         (e.g. the enchanted player for Curse of Oblivion); null = the controller
 */
public record ExileGraveyardCardsEffect(
        int count, GraveyardExileScope scope, CardPredicate filter, UUID affectedPlayerId) implements CardEffect {

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope) {
        this(count, scope, null, null);
    }

    public ExileGraveyardCardsEffect(int count, GraveyardExileScope scope, CardPredicate filter) {
        this(count, scope, filter, null);
    }

    public ExileGraveyardCardsEffect(GraveyardExileScope scope) {
        this(0, scope, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        // Per-scope spec reproducing the old conditional booleans exactly:
        //   TARGET_CARDS_ANY_GRAVEYARD      -> (graveyard=T, any=T)    -> ANY_GRAVEYARD_CARD
        //   TARGET_CARDS_OPPONENT_GRAVEYARD -> (graveyard=T, any=F)    -> GRAVEYARD_CARD
        //   TARGET_PLAYER_ENTIRE            -> (player=T)              -> PLAYER
        //   OWN / ALL_PLAYERS / ALL_OPPONENTS -> no single validated target -> NONE
        return switch (scope) {
            case TARGET_CARDS_ANY_GRAVEYARD -> TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
            case TARGET_CARDS_OPPONENT_GRAVEYARD -> TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.OPPONENT_GRAVEYARD));
            case TARGET_PLAYER_ENTIRE, TARGET_PLAYER_ALL_MATCHING -> TargetSpec.benign(TargetPredicates.player());
            default -> TargetSpec.NONE;
        };
    }
}
