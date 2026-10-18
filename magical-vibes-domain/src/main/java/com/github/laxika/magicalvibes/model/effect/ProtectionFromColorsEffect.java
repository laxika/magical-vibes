package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Grants protection from the specified colors.
 *
 * @param colors         the colors to protect from
 * @param scope          {@code null} means the permanent itself has protection (e.g. Black Knight);
 *                       {@link GrantScope#EQUIPPED_CREATURE} means the equipped creature has protection
 *                       (e.g. Sword of War and Peace). The scope is used by
 *                       {@code GameQueryService.hasProtectionFrom()} to decide whether the raw effect
 *                       applies to the card that carries it.
 * @param doesNotRemoveAuras {@code true} for "This effect doesn't remove Auras" (Spectra Ward): an
 *                       Aura already attached to the creature with this protection stays attached.
 */
public record ProtectionFromColorsEffect(Set<CardColor> colors, GrantScope scope,
                                         boolean doesNotRemoveAuras)
        implements ProtectionGrantingEffect {

    public ProtectionFromColorsEffect(Set<CardColor> colors, GrantScope scope) {
        this(colors, scope, false);
    }

    /**
     * Convenience constructor for creatures that themselves have protection (scope = null / self).
     */
    public ProtectionFromColorsEffect(Set<CardColor> colors) {
        this(colors, null, false);
    }

    @Override
    public Set<CardColor> protectionFromColors() {
        return colors;
    }

    @Override
    public GrantScope protectionScope() {
        return scope;
    }
}
