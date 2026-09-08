package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect (self): all damage that would be dealt to this permanent's controller is dealt
 * to this permanent instead (e.g. Empyrial Archangel). Unlike
 * {@link RedirectPlayerDamageToEnchantedCreatureEffect}, the redirect target is the source
 * permanent itself rather than an enchanted creature.
 *
 * <p>When {@code includeOtherPermanents} is set, damage that would be dealt to the other
 * permanents that player controls is redirected as well (e.g. Palisade Giant). When
 * {@code onlyFromUnblockedCreatures} is set, the player-half applies only to damage from an
 * unblocked attacking creature (e.g. Veteran Bodyguard). Damage that would be dealt to this
 * permanent itself is never redirected.
 */
public record RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents,
                                               boolean onlyFromUnblockedCreatures) implements CardEffect {

    public RedirectPlayerDamageToSelfEffect() {
        this(false, false);
    }

    public RedirectPlayerDamageToSelfEffect(boolean includeOtherPermanents) {
        this(includeOtherPermanents, false);
    }
}
