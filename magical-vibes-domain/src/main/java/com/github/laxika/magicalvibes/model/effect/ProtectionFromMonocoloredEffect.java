package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants protection from sources with exactly one color.
 */
public record ProtectionFromMonocoloredEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromMonocolored() {
        return true;
    }
}
