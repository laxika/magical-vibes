package com.github.laxika.magicalvibes.model.effect;

/**
 * The affected creature cannot attack this effect's controller, but may still attack that player's
 * planeswalkers ("can't attack you", unlike {@link EnchantedCreatureCantAttackControllerEffect}).
 */
public record EnchantedCreatureCantAttackPlayerEffect() implements EnchantedCreatureAttackRestrictionEffect {
}
