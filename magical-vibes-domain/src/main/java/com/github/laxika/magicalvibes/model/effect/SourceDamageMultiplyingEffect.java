package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that multiplies damage dealt by matching sources. By default, matching sources
 * must be controlled by the permanent carrying this effect.
 */
public interface SourceDamageMultiplyingEffect extends DoublingEffect {

    int damageMultiplier();

    PermanentPredicate sourceFilter();

    /** Whether this multiplier also applies when the matching source deals noncombat damage. */
    default boolean appliesToNonCombatDamage() {
        return true;
    }

    /** Whether this multiplier applies to combat damage aimed at the supplied permanent ({@code null}: a player). */
    default boolean appliesToCombatDamageTarget(Permanent target) {
        return true;
    }

    /** Whether matching sources must be controlled by the permanent carrying this effect. */
    default boolean requiresSourceControllerMatch() {
        return true;
    }

    /**
     * What a spell dealing damage itself must match, judged on its card with its X, to be a matching source; {@code null}
     * when no spell is.
     */
    default CardPredicate spellSourceFilter() {
        return null;
    }
}
