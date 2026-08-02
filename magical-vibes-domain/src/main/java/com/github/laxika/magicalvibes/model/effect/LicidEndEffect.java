package com.github.laxika.magicalvibes.model.effect;

/**
 * The "You may pay [cost] to end this effect" half of a Licid ability: the source detaches and
 * reverts to its printed creature form.
 *
 * <p>Never written on a card class — {@link LicidBecomeAuraEffect}'s handler installs the
 * activated ability carrying this effect on the Aura-form runtime copy.</p>
 */
public record LicidEndEffect() implements CardEffect {
}
