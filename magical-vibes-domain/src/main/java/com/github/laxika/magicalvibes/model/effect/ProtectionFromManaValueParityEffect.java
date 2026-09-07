package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants protection from sources whose mana value matches the source permanent's chosen odd/even
 * quality. No protection is granted until that quality has been chosen.
 */
public record ProtectionFromManaValueParityEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromManaValueParity() {
        return true;
    }
}
