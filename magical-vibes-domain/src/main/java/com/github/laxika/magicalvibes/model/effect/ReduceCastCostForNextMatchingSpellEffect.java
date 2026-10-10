package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Creates a generic cost reduction for the controller's next matching spell.
 *
 * <p>{@code expiresAtCleanup} selects the duration: {@code true} makes the reduction wear off at the
 * cleanup step if no matching spell was cast this turn ("the next ... spell you cast this turn",
 * Invasion of the Giants); {@code false} keeps it until the next matching spell is cast, across
 * turns ("the next ... spell you cast", Draconic Debut).</p>
 */
public record ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, DynamicAmount amount,
                                                       boolean faceDownOnly, boolean expiresAtCleanup)
        implements NextMatchingSpellCostEffect {

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, DynamicAmount amount) {
        this(predicate, amount, false, true);
    }

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, int amount) {
        this(predicate, new Fixed(amount), false, true);
    }

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, int amount,
                                                    boolean faceDownOnly) {
        this(predicate, new Fixed(amount), faceDownOnly, true);
    }

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, DynamicAmount amount,
                                                    boolean faceDownOnly) {
        this(predicate, amount, faceDownOnly, true);
    }

    @Override
    public boolean appliesToFaceDownCast(boolean castFaceDown) {
        return !faceDownOnly || castFaceDown;
    }
}
