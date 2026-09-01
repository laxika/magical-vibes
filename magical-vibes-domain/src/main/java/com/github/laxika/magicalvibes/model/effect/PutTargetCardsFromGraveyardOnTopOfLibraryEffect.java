package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Put target cards matching the filter from a graveyard on top of that player's library.
 * {@code maxTargets} caps how many may be chosen; {@link #ANY_NUMBER} means "any number".
 * {@code source} selects whose graveyard the targets come from: the controller's own
 * ({@link GraveyardSearchScope#CONTROLLERS_GRAVEYARD}, the default), a single opponent's
 * ({@link GraveyardSearchScope#OPPONENT_GRAVEYARD}, Misinformation) or any single player's
 * ({@link GraveyardSearchScope#ALL_GRAVEYARDS}, Lodestone Bauble). The two non-default scopes put
 * each card on top of its own owner's library.
 * Multi-target graveyard selection is handled by SpellCastingService at cast time; for an activated
 * ability the targets ride in on the activation's {@code targetIds} with {@code Zone.GRAVEYARD}.
 */
public record PutTargetCardsFromGraveyardOnTopOfLibraryEffect(
        CardPredicate filter,
        int maxTargets,
        GraveyardSearchScope source
) implements TargetedGraveyardCardsEffect {

    /** Sentinel for "any number of target cards" (capped only by the graveyard contents). */
    public static final int ANY_NUMBER = 0;

    public PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    public PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardPredicate filter) {
        this(filter, ANY_NUMBER, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** "Put up to {@code maxTargets} target cards from an opponent's graveyard on top of their library." */
    public static PutTargetCardsFromGraveyardOnTopOfLibraryEffect fromOpponentGraveyard(
            CardPredicate filter, int maxTargets) {
        return new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(filter, maxTargets,
                GraveyardSearchScope.OPPONENT_GRAVEYARD);
    }

    /** "Put up to {@code maxTargets} target cards from a player's graveyard on top of their library." */
    public static PutTargetCardsFromGraveyardOnTopOfLibraryEffect fromAnyPlayerGraveyard(
            CardPredicate filter, int maxTargets) {
        return new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(filter, maxTargets,
                GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    /** Whether the chosen cards live in someone else's graveyard than the controller's. */
    public boolean fromOtherGraveyards() {
        return source != GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, source));
    }
}
