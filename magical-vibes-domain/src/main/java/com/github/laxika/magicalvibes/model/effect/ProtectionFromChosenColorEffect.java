package com.github.laxika.magicalvibes.model.effect;

/**
 * Protection from the colour chosen for the source permanent ({@code Permanent.getChosenColor()}), a static ability.
 * The choice itself is a separate ability, such as {@link ChooseColorOnEnterEffect} ("As this enters, choose a
 * color").
 *
 * @param scope {@code null} means the permanent itself has the protection (Voice of All).
 *              {@link GrantScope#ENCHANTED_CREATURE} or {@link GrantScope#EQUIPPED_CREATURE} means the creature
 *              this Aura or Equipment is attached to has it (Ward of Lights, Sanctuary Blade). An Aura
 *              carrying the enchanted-creature scope is never made illegally attached by the
 *              protection it grants ("This effect doesn't remove this Aura").
 */
public record ProtectionFromChosenColorEffect(GrantScope scope) implements CardEffect {

    /** Convenience constructor for the self-scoped shape (Voice of All). */
    public ProtectionFromChosenColorEffect() {
        this(null);
    }
}
