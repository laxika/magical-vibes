package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "Creatures you control enter as a copy of this creature."
 * When any creature would enter the battlefield under the controller of the permanent
 * bearing this effect, it enters as a copy of that permanent instead (CR 614.1c).
 *
 * <p>{@code copyEnchantedCreature} switches the copied permanent from the source itself to
 * the creature the source Aura is attached to ("Nontoken creatures you control enter as a
 * copy of enchanted creature" — Infinite Reflection); nothing happens while the source is
 * unattached. {@code nontokenOnly} restricts the replacement to nontoken entering creatures.
 */
public record CreaturesEnterAsCopyOfSourceEffect(boolean copyEnchantedCreature, boolean nontokenOnly)
        implements CardEffect {

    public CreaturesEnterAsCopyOfSourceEffect() {
        this(false, false);
    }
}
