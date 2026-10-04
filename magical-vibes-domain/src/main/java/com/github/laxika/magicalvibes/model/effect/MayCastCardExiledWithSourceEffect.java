package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast a spell from among cards exiled with this permanent" (normally without paying its
 * mana cost). An optional {@code manaValue} restricts the offered spells to cards with exactly
 * that mana value. The {@link #normalCost(CardPredicate, DynamicAmount)} factory supports a
 * normal-cost offer with a dynamic generic reduction.
 *
 * <p>The <em>ability's controller</em> — not the exiled card's owner — is offered the cast, and
 * only one of the exiled cards may be cast. When {@code random} is true, one eligible card is
 * selected at random before the single may-cast offer is created. Contrast
 * {@link MayCastCardsExiledWithSourceEffect},
 * which offers <em>every</em> card exiled with a departing permanent to its own owner (Spell
 * Queller).</p>
 */
public record MayCastCardExiledWithSourceEffect(DynamicAmount manaValue, CardPredicate filter,
                                                boolean random, boolean withoutPayingManaCost,
                                                DynamicAmount genericCostReduction, boolean ownOnly) implements CardEffect {

    public MayCastCardExiledWithSourceEffect(DynamicAmount manaValue, CardPredicate filter,
                                            boolean random, boolean withoutPayingManaCost,
                                            DynamicAmount genericCostReduction) {
        this(manaValue, filter, random, withoutPayingManaCost, genericCostReduction, false);
    }

    public MayCastCardExiledWithSourceEffect() {
        this(null, null, false, true, null);
    }

    public MayCastCardExiledWithSourceEffect(DynamicAmount manaValue) {
        this(manaValue, null, false, true, null);
    }

    public MayCastCardExiledWithSourceEffect(CardPredicate filter) {
        this(null, filter, false, true, null);
    }

    public MayCastCardExiledWithSourceEffect(DynamicAmount manaValue, CardPredicate filter) {
        this(manaValue, filter, false, true, null);
    }

    public MayCastCardExiledWithSourceEffect(boolean random) {
        this(null, null, random, true, null);
    }

    public static MayCastCardExiledWithSourceEffect randomSelection() {
        return new MayCastCardExiledWithSourceEffect(null, null, true, true, null);
    }

    public static MayCastCardExiledWithSourceEffect normalCost(CardPredicate filter,
                                                                DynamicAmount genericCostReduction) {
        return new MayCastCardExiledWithSourceEffect(null, filter, false, false,
                genericCostReduction);
    }
}
