package com.github.laxika.magicalvibes.model.effect;

/** Grants protection from sources with exactly two enemy colors. */
public record ProtectionFromEnemyColoredMulticoloredEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromEnemyColoredMulticolored() {
        return true;
    }
}
