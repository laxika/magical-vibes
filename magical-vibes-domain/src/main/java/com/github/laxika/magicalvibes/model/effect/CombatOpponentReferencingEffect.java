package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for effects that, when fired from an attached permanent (aura/equipment) as a
 * combat trigger, act on the enchanted/equipped creature's combat opponent — the creature it blocks
 * or that becomes blocked by it. When {@code CombatTriggerService} routes such an effect it passes the
 * opponent as the trigger's non-targeting target (so the trigger can't fizzle) rather than letting the
 * controller choose a target. Basilisk-style auras such as Venom use this.
 *
 * <p>Descriptive only: membership is a fact about the effect, never a score.
 */
public interface CombatOpponentReferencingEffect extends CardEffect {

    /**
     * Whether this instance actually acts on the combat opponent. Effects whose scope decides
     * where they apply (e.g. {@link GrantKeywordEffect}, which only references the opponent with
     * {@link GrantScope#TARGET}) override this so the other scopes keep resolving against the
     * source/its controller instead of being auto-targeted.
     */
    default boolean referencesCombatOpponent() {
        return true;
    }
}
