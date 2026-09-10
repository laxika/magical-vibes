package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect (self): all damage that would be dealt to this permanent's controller is dealt
 * to this permanent instead (e.g. Empyrial Archangel). Unlike
 * {@link RedirectPlayerDamageToEnchantedCreatureEffect}, the redirect target is the source
 * permanent itself rather than an enchanted creature.
 *
 * <p>When {@code includeOtherPermanents} is set, damage that would be dealt to the other
 * permanents that player controls is redirected as well (e.g. Palisade Giant). When
 * {@code onlyFromUnblockedCreatures} is set, the player-half applies only to damage from an
 * unblocked attacking creature (e.g. Veteran Bodyguard). When {@code sourcePredicate} is set,
 * only damage from matching sources is redirected; {@code requiresUntapped} makes the effect
 * active only while this permanent is untapped (e.g. Martyrs of Korlis). Damage that would be
 * dealt to this permanent itself is never redirected.
 */
public record RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents,
                                               boolean onlyFromUnblockedCreatures,
                                               PermanentPredicate sourcePredicate,
                                               boolean requiresUntapped) implements CardEffect {

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
