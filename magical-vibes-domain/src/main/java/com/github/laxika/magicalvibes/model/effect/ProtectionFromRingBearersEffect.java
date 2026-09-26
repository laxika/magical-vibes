package com.github.laxika.magicalvibes.model.effect;

/**
 * Static protection from permanents currently designated as Ring-bearers.
 *
 * <p>The protected set is derived from live Ring state rather than from a fixed card
 * characteristic, so the battlefield query layer evaluates it against the source permanent.
 */
public record ProtectionFromRingBearersEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromRingBearers() {
        return true;
    }
}
