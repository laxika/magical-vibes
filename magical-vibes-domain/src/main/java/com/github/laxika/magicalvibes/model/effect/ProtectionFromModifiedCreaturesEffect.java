package com.github.laxika.magicalvibes.model.effect;

/** Grants protection from modified creature permanents. */
public record ProtectionFromModifiedCreaturesEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromModifiedCreatures() {
        return true;
    }
}
