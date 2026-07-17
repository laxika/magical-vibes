package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect (self): all damage that would be dealt to this permanent's controller is dealt
 * to this permanent instead (e.g. Empyrial Archangel). Unlike
 * {@link RedirectPlayerDamageToEnchantedCreatureEffect}, the redirect target is the source
 * permanent itself rather than an enchanted creature.
 */
public record RedirectPlayerDamageToSelfEffect() implements CardEffect {
}
