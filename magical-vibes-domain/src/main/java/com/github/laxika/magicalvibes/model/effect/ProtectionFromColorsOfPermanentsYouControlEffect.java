package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC, scope-aware: the carrying permanent or enchanted creature has protection from the colors of the permanents its
 * controller controls (Empty-Shrine Kannushi). The protected color set is not a function of this
 * record's components — it is recomputed from the battlefield every time it is asked — so unlike
 * {@link ProtectionFromColorsEffect} this shape deliberately does not implement
 * {@link ProtectionGrantingEffect}; {@code GameQueryService.hasProtectionFrom} evaluates it
 * directly against the controller's permanents for the self scope; the enchanted-creature scope
 * is applied by its static handler (layer-5 aware, so color changes are seen).
 */
public record ProtectionFromColorsOfPermanentsYouControlEffect(GrantScope scope) implements CardEffect {

    public ProtectionFromColorsOfPermanentsYouControlEffect() {
        this(null);
    }
}
