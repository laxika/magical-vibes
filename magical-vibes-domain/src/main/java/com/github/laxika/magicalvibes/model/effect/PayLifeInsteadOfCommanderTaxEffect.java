package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability of a commander: rather than pay {2} for each previous time its owner cast it from
 * the command zone, its caster pays {@code lifePerPreviousCast} life that many times
 * (Liesa, Shroud of Dusk). The mana part of the commander tax is removed from the cost and the life
 * is paid as the spell is cast; a caster who cannot pay it cannot cast the commander.
 *
 * @param lifePerPreviousCast the life paid in place of each {2} of commander tax
 */
public record PayLifeInsteadOfCommanderTaxEffect(int lifePerPreviousCast) implements CardEffect {
}
