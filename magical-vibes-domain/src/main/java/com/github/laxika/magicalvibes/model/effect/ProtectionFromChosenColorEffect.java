package com.github.laxika.magicalvibes.model.effect;

/**
 * Protection from the colour chosen for the source permanent ({@code Permanent.getChosenColor()}).
 *
 * @param scope {@code null} means the permanent itself has the protection — declare it in
 *              {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ENTER_BATTLEFIELD}, where it
 *              also performs the colour choice (Voice of All).
 *              {@link GrantScope#ENCHANTED_CREATURE} means the creature this Aura is attached to has
 *              it — declare it in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC} and
 *              pair with a {@link ChooseColorOnEnterEffect} for the choice (Ward of Lights). An Aura
 *              carrying the enchanted-creature scope is never made illegally attached by the
 *              protection it grants ("This effect doesn't remove this Aura").
 */
public record ProtectionFromChosenColorEffect(GrantScope scope) implements ChooseColorEffect {

    /** Convenience constructor for the self-scoped shape (Voice of All). */
    public ProtectionFromChosenColorEffect() {
        this(null);
    }
}
