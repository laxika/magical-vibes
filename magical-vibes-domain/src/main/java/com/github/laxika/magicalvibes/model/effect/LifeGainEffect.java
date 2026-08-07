package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Capability interface for effects that make a player gain a single evaluated amount of life. Lets
 * consumers — chiefly the AI evaluators/classifiers — ask "how much life does this gain" without
 * knowing the concrete effect type, mirroring how {@link ManaProducingEffect} abstracts mana
 * production.
 *
 * <p>Descriptive only: it states a fact drawn from the record's existing components, never a score.
 */
public interface LifeGainEffect extends CardEffect {

    /**
     * The amount of life gained, as a {@link DynamicAmount} evaluated at resolution (fixed number,
     * X paid, "for each …", …).
     */
    DynamicAmount lifeGainAmount();

    /**
     * True when this configuration gains no life at all, which its own components already settle —
     * an effect whose life gain is an optional rider reports {@code Fixed(0)} when the rider is off
     * ({@link RevealTopCardOfLibraryEffect} without a land bonus). A consumer that only asks "does
     * this card gain life" must consult this rather than {@code instanceof LifeGainEffect}, or it
     * scores a plain reveal as a lifegain spell.
     */
    default boolean gainsNoLife() {
        return lifeGainAmount() instanceof Fixed fixed && fixed.value() == 0;
    }
}
