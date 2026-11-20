package com.github.laxika.magicalvibes.model.effect;

/**
 * Protection from the colour chosen for the source permanent ({@code Permanent.getChosenColor()}), a static ability.
 * The choice itself is a separate ability, such as {@link ChooseColorOnEnterEffect} ("As this enters, choose a
 * color").
 *
 * @param subject {@code null} means the permanent itself has the protection (Voice of All).
 *              {@link Subjects#attachedCreature()} or {@link Subjects#attachedCreature()} means the creature
 *              this Aura or Equipment is attached to has it (Ward of Lights, Sanctuary Blade). An Aura
 *              carrying the enchanted-creature subject is never made illegally attached by the
 *              protection it grants ("This effect doesn't remove this Aura").
 */
public record ProtectionFromChosenColorEffect(PermanentSubject subject) implements CardEffect {

    /** Convenience constructor for the self-scoped shape (Voice of All). */
    public ProtectionFromChosenColorEffect() {
        this(null);
    }
}
