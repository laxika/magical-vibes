package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * A self-contained, Spring-managed cast-cost modification handler.
 *
 * <p>Each cost-modifying static effect lives in its own {@code @Component} implementing this
 * interface (mirroring {@code StaticEffectHandlerBean}). The handler declares which
 * {@link CardEffect} type it handles via {@link #handledEffect()} and whether the effect is
 * read from the spell's own static effects ({@link #onSpellItself()}) or from static effects
 * on battlefield permanents (the default).
 *
 * <p>{@link #modifyCost} returns a signed generic-mana delta: positive means the spell costs
 * more, negative means it costs less, zero means this occurrence doesn't apply. The handler is
 * responsible for its own scoping (e.g. "only when the source permanent is controlled by the
 * casting player") via {@link CostModificationSource}.
 *
 * <p>Adding a new cost-modifier card means adding exactly one new handler class here —
 * {@code CastingCostService} picks it up automatically for both the UI cost preview and the
 * actual cast-time payment.
 */
public interface CostModificationHandlerBean {

    Class<? extends CardEffect> handledEffect();

    /**
     * True if this effect is carried by the spell being cast itself (e.g. "this spell costs
     * {1} less for each creature on the battlefield") rather than by a battlefield permanent.
     */
    default boolean onSpellItself() {
        return false;
    }

    int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source);
}
