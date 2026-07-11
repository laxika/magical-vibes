package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static marker restricting which spells and/or abilities may target this permanent. Scanned at
 * targeting time by the target-legality services (never resolved on the stack).
 * <p>
 * Collapses the former "this permanent can't be targeted by X" record family:
 * <ul>
 *   <li>{@link #opponentAbilities()} — opponents' abilities can't target (Shanna, Sisay's Legacy).</li>
 *   <li>{@link #hexproof()} — opponents' spells and abilities can't target (granted by Asceticism).</li>
 *   <li>{@link #fromNonColorSources(CardColor)} — only sources of the given color may target (Gaea's Revenge).</li>
 *   <li>{@link #fromSpellColors(Set)} — spells of the given colors can't target (Karplusan Strider).</li>
 *   <li>{@link #hexproofFromColors(Set)} — opponents' spells/abilities of the given colors can't target
 *       (Knight of Grace/Malice — hexproof from color, CR 702.11).</li>
 * </ul>
 *
 * @param kind         which source kinds the restriction covers
 * @param opponentOnly whether the restriction only blocks opponent-controlled sources (hexproof-style)
 *                     or everyone including the controller
 * @param colors       the colors relevant to {@code mode} (empty when {@code mode == ANY})
 * @param mode         how {@code colors} is interpreted
 */
public record TargetingRestrictionEffect(
        TargetingSourceKind kind,
        boolean opponentOnly,
        Set<CardColor> colors,
        TargetColorMode mode) implements CardEffect {

    /** Opponents' abilities can't target this permanent (spells still can). Shanna, Sisay's Legacy. */
    public static TargetingRestrictionEffect opponentAbilities() {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.ABILITIES, true, Set.of(), TargetColorMode.ANY);
    }

    /** Hexproof — opponents' spells and abilities can't target this permanent. Granted by Asceticism. */
    public static TargetingRestrictionEffect hexproof() {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, true, Set.of(), TargetColorMode.ANY);
    }

    /** Can't be the target of spells or abilities from sources that are not the given color. Gaea's Revenge. */
    public static TargetingRestrictionEffect fromNonColorSources(CardColor allowedColor) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, false, Set.of(allowedColor),
                TargetColorMode.ALLOWED_COLORS_ONLY);
    }

    /** Can't be the target of spells of the given colors. Karplusan Strider. */
    public static TargetingRestrictionEffect fromSpellColors(Set<CardColor> colors) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS, false, colors, TargetColorMode.BLOCKED_COLORS);
    }

    /** Hexproof from the given colors — opponents' colored spells/abilities can't target. Knight of Grace/Malice. */
    public static TargetingRestrictionEffect hexproofFromColors(Set<CardColor> colors) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, true, colors, TargetColorMode.BLOCKED_COLORS);
    }
}
