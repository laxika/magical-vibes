package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaValueParity;

/**
 * Grants protection from each mana value of an odd/even quality (CR 702.16i). No protection is granted until that
 * quality is known.
 *
 * @param parity the quality, when the effect fixes it (a shared copy of the ability); {@code null} for the quality
 *               chosen for the permanent that has the ability
 */
public record ProtectionFromManaValueParityEffect(ManaValueParity parity) implements ProtectionGrantingEffect {

    /** Protection from each mana value of the quality chosen for the permanent that has the ability. */
    public ProtectionFromManaValueParityEffect() {
        this(null);
    }

    @Override
    public boolean protectionFromManaValueParity() {
        return true;
    }

    @Override
    public ManaValueParity protectionFromFixedManaValueParity() {
        return parity;
    }
}
