package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants "protection from everything" (e.g. Progenitus): protection from every source, regardless
 * of its colour, card type, subtype or mana value. Static, permanent. Surfaced through
 * {@link ProtectionGrantingEffect#protectsFromEverything()} and consulted by
 * {@code GameQueryService.hasProtectionFromSourceCardTypes()}, the shared gate for damage, combat,
 * targeting and enchant/equip.
 */
public record ProtectionFromEverythingEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectsFromEverything() {
        return true;
    }
}
