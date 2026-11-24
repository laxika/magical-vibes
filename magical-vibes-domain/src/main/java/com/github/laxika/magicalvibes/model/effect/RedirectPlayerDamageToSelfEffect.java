package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect (self): all damage that would be dealt to this permanent's controller is dealt
 * to this permanent instead (e.g. Empyrial Archangel), or with {@link #toAttachedPermanent()} to the
 * creature this Aura or Equipment is attached to (Pariah).
 *
 * <p>When {@code includeOtherPermanents} is set, damage that would be dealt to the other
 * permanents that player controls is redirected as well (e.g. Palisade Giant). When
 * {@code onlyFromUnblockedCreatures} is set, the player-half applies only to damage from an
 * unblocked attacking creature (e.g. Veteran Bodyguard). When {@code sourcePredicate} is set,
 * only damage from matching sources is redirected; {@code requiresUntapped} makes the effect
 * active only while this permanent is untapped (e.g. Martyrs of Korlis); {@code combatOnly} limits it to combat
 * damage (Weathered Bodyguards). Damage that would be dealt to this permanent itself is never redirected.
 *
 * <p>Floated on a player by {@link GrantStaticEffectToPlayerEffect} (Kjeldoran Royal Guard's "this turn"), "this
 * creature" is the floating effect's source permanent; the redirection does nothing once that permanent has left the
 * battlefield or is no longer a creature or planeswalker (CR 614.9).
 */
public record RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents,
                                               boolean onlyFromUnblockedCreatures,
                                               PermanentPredicate sourcePredicate,
                                               boolean requiresUntapped,
                                               PermanentSubject subject,
                                               boolean combatOnly) implements CardEffect {

    public RedirectPlayerDamageToSelfEffect {
        if (!(subject instanceof PermanentSubject.Source)
                && (!(subject instanceof PermanentSubject.Attached) || includeOtherPermanents
                || onlyFromUnblockedCreatures || sourcePredicate != null || requiresUntapped || combatOnly)) {
            throw new IllegalArgumentException("Redirects to the source, or all of the damage to its host: " + subject);
        }
    }

    public RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents, boolean onlyFromUnblockedCreatures,
                                            PermanentPredicate sourcePredicate, boolean requiresUntapped) {
        this(includeOtherPermanents, onlyFromUnblockedCreatures, sourcePredicate, requiresUntapped, Subjects.source(),
                false);
    }

    /** "All damage that would be dealt to you is dealt to enchanted (equipped) creature instead." */
    public static RedirectPlayerDamageToSelfEffect toAttachedPermanent() {
        return new RedirectPlayerDamageToSelfEffect(false, false, null, false, Subjects.attachedCreature(), false);
    }

    /**
     * "All combat damage that would be dealt to you by unblocked creatures is dealt to this creature instead", only
     * while it is untapped when {@code requiresUntapped} (Weathered Bodyguards' static ability; Kjeldoran Royal Guard
     * floats it on its controller this turn, untapped or not). Trample damage of a blocked creature is not redirected.
     */
    public static RedirectPlayerDamageToSelfEffect combatDamageFromUnblockedCreatures(boolean requiresUntapped) {
        return new RedirectPlayerDamageToSelfEffect(false, true, null, requiresUntapped, Subjects.source(), true);
    }

    public RedirectPlayerDamageToSelfEffect() {
        this(false, false, null, false);
    }

    public RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents) {
        this(includeOtherPermanents, false, null, false);
    }

    public RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents,
                                            boolean onlyFromUnblockedCreatures) {
        this(includeOtherPermanents, onlyFromUnblockedCreatures, null, onlyFromUnblockedCreatures);
    }

    public RedirectPlayerDamageToSelfEffect(PermanentPredicate sourcePredicate) {
        this(false, false, sourcePredicate, false);
    }

    public RedirectPlayerDamageToSelfEffect(PermanentPredicate sourcePredicate, boolean requiresUntapped) {
        this(false, false, sourcePredicate, requiresUntapped);
    }
}
