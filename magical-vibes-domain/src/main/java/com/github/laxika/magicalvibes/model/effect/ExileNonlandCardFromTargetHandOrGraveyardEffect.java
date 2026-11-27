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
 *                            remains exiled, and mana of any type can be spent to cast it
 * @param handOnly whether cards from the graveyard are not eligible
 * @param handFilter filter for cards in the target player's hand
 * @param graveyardFilter filter for cards in the target player's graveyard
 * @param revealMatchingHand whether to reveal only matching hand cards instead of the entire hand
 * @param manaAsThoughAnyColor with {@code grantPlayPermission}, "you may spend mana as though it were mana of any
 *                             color to cast that spell" instead of any type: a {C} symbol is still paid only with
 *                             colorless mana (CR 107.4c)
 */
public record ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission, boolean handOnly,
                                                               CardPredicate handFilter,
                                                               CardPredicate graveyardFilter,
                                                               boolean revealMatchingHand,
                                                               boolean manaAsThoughAnyColor)
        implements CardEffect {

    public ExileNonlandCardFromTargetHandOrGraveyardEffect {
        Objects.requireNonNull(handFilter, "handFilter");
        Objects.requireNonNull(graveyardFilter, "graveyardFilter");
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission, boolean handOnly,
                                                            CardPredicate handFilter, CardPredicate graveyardFilter,
                                                            boolean revealMatchingHand) {
        this(grantPlayPermission, handOnly, handFilter, graveyardFilter, revealMatchingHand, false);
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect() {
        this(true, false, nonlandFilter(), nonlandFilter(), false);
    }

    /** "… and you may spend mana as though it were mana of any color to cast that spell" (not {C}, CR 107.4c). */
    public ExileNonlandCardFromTargetHandOrGraveyardEffect withManaAsThoughAnyColor() {
        return new ExileNonlandCardFromTargetHandOrGraveyardEffect(grantPlayPermission, handOnly, handFilter,
                graveyardFilter, revealMatchingHand, true);
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
