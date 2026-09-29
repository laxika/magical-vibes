package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;

/**
 * Reveals the target player's hand, then lets the controller choose a matching card from that
 * hand or that player's graveyard and exile it.
 *
 * @param grantPlayPermission whether the controller may cast the exiled card for as long as it
 *                            remains exiled
 * @param handOnly whether cards from the graveyard are not eligible
 * @param handFilter filter for cards in the target player's hand
 * @param graveyardFilter filter for cards in the target player's graveyard
 * @param revealMatchingHand whether to reveal only matching hand cards instead of the entire hand
 */
public record ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission, boolean handOnly,
                                                               CardPredicate handFilter,
                                                               CardPredicate graveyardFilter,
                                                               boolean revealMatchingHand)
        implements CardEffect {

    public ExileNonlandCardFromTargetHandOrGraveyardEffect {
        Objects.requireNonNull(handFilter, "handFilter");
        Objects.requireNonNull(graveyardFilter, "graveyardFilter");
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect() {
        this(true, false, nonlandFilter(), nonlandFilter(), false);
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission) {
        this(grantPlayPermission, false, nonlandFilter(), nonlandFilter(), false);
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission, boolean handOnly) {
        this(grantPlayPermission, handOnly, nonlandFilter(), nonlandFilter(), false);
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission, boolean handOnly,
                                                            CardPredicate handFilter,
                                                            CardPredicate graveyardFilter) {
        this(grantPlayPermission, handOnly, handFilter, graveyardFilter, false);
    }

    private static CardPredicate nonlandFilter() {
        return new CardNotPredicate(new CardTypePredicate(CardType.LAND));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
