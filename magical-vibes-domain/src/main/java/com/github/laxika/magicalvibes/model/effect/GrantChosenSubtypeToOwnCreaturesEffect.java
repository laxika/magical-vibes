package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each creature you control is the chosen type in addition to its other types.
 * Reads the chosen subtype from the source permanent's {@code chosenSubtype} field.
 * Used by Xenograft, Arcane Adaptation, and similar effects.
 *
 * @param affectsAllZones when {@code true}, also applies to creature spells you control on the
 *                        stack and creature cards you own that aren't on the battlefield (hand,
 *                        graveyard, library, exile). Used by Arcane Adaptation.
 */
public record GrantChosenSubtypeToOwnCreaturesEffect(boolean affectsAllZones) implements CardEffect {

    /** Battlefield-only variant (e.g. Xenograft). */
    public GrantChosenSubtypeToOwnCreaturesEffect() {
        this(false);
    }
}
