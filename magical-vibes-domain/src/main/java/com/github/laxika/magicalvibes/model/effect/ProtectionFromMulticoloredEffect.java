package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants protection from sources with two or more colors.
 *
 * @param subject {@code null} means the permanent itself has protection;
 *              {@link Subjects#attachedCreature()} means the enchanted creature has it
 */
public record ProtectionFromMulticoloredEffect(PermanentSubject subject)
        implements ProtectionGrantingEffect {

    public ProtectionFromMulticoloredEffect() {
        this(null);
    }

    @Override
    public boolean protectionFromMulticolored() {
        return true;
    }

    @Override
    public PermanentSubject protectionSubject() {
        return subject;
    }
}
