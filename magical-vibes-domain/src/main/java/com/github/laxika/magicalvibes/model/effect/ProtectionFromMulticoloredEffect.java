package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants protection from sources with two or more colors.
 *
 * @param scope {@code null} means the permanent itself has protection;
 *              {@link GrantScope#ENCHANTED_CREATURE} means the enchanted creature has it
 */
public record ProtectionFromMulticoloredEffect(GrantScope scope)
        implements ProtectionGrantingEffect {

    public ProtectionFromMulticoloredEffect() {
        this(null);
    }

    @Override
    public boolean protectionFromMulticolored() {
        return true;
    }

    @Override
    public GrantScope protectionScope() {
        return scope;
    }
}
