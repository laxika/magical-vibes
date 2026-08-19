package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;

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
 *   <li>{@link #spells()} — no spell may target, abilities still can (Dense Foliage).</li>
 *   <li>{@link #fromSpellColors(Set)} — spells of the given colors can't target (Karplusan Strider).</li>
 *   <li>{@link #hexproofFromColors(Set)} — opponents' spells/abilities of the given colors can't target
 *       (Knight of Grace/Malice — hexproof from color, CR 702.11).</li>
 *   <li>{@link #fromSourceColors(Set)} — spells/abilities of the given colors can't target, no matter
 *       who controls them (Suq'Ata Firewalker).</li>
 *   <li>{@link #hexproofFromCardTypes(Set)} — opponents' spells of the given card types can't target
 *       this permanent (Elenda, Saint of Dusk).</li>
 * </ul>
 *
 * @param kind         which source kinds the restriction covers
 * @param opponentOnly whether the restriction only blocks opponent-controlled sources (hexproof-style)
 *                     or everyone including the controller
 * @param colors       the colors relevant to {@code mode} (empty when {@code mode == ANY})
 * @param mode         how {@code colors} is interpreted
 * @param hexproofLike whether hexproof-ignoring effects may bypass this restriction
 * @param sourceCardTypes card types relevant to a card-type restriction; empty for color-based restrictions
 */
public record TargetingRestrictionEffect(
        TargetingSourceKind kind,
        boolean opponentOnly,
        Set<CardColor> colors,
        TargetColorMode mode,
        boolean hexproofLike,
        Set<CardType> sourceCardTypes) implements CardEffect {

    public TargetingRestrictionEffect(TargetingSourceKind kind, boolean opponentOnly,
                                      Set<CardColor> colors, TargetColorMode mode) {
        this(kind, opponentOnly, colors, mode, false, Set.of());
    }

    public TargetingRestrictionEffect(TargetingSourceKind kind, boolean opponentOnly,
                                      Set<CardColor> colors, TargetColorMode mode,
                                      boolean hexproofLike) {
        this(kind, opponentOnly, colors, mode, hexproofLike, Set.of());
    }

    public TargetingRestrictionEffect(TargetingSourceKind kind, boolean opponentOnly,
                                      Set<CardColor> colors, TargetColorMode mode,
                                      Set<CardType> sourceCardTypes) {
        this(kind, opponentOnly, colors, mode, true, sourceCardTypes);
    }

    public TargetingRestrictionEffect {
        colors = Set.copyOf(colors);
        sourceCardTypes = Set.copyOf(sourceCardTypes);
    }

    /** Opponents' abilities can't target this permanent (spells still can). Shanna, Sisay's Legacy. */
    public static TargetingRestrictionEffect opponentAbilities() {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.ABILITIES, true, Set.of(), TargetColorMode.ANY, false);
    }

    /** Hexproof — opponents' spells and abilities can't target this permanent. Granted by Asceticism. */
    public static TargetingRestrictionEffect hexproof() {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, true, Set.of(), TargetColorMode.ANY, true);
    }

    /** Opponents' spells and abilities can't target this permanent, without granting hexproof. */
    public static TargetingRestrictionEffect opponentSpellsAndAbilities() {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, true, Set.of(), TargetColorMode.ANY, false);
    }

    /** Can't be the target of spells or abilities from sources that are not the given color. Gaea's Revenge. */
    public static TargetingRestrictionEffect fromNonColorSources(CardColor allowedColor) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, false, Set.of(allowedColor),
                TargetColorMode.ALLOWED_COLORS_ONLY, false);
    }

    /** Can't be the target of spells or abilities from disallowed-color sources controlled by opponents. */
    public static TargetingRestrictionEffect fromOpponentNonColorSources(CardColor allowedColor) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, true, Set.of(allowedColor),
                TargetColorMode.ALLOWED_COLORS_ONLY, false);
    }

    /** Can't be the target of spells (any color, any controller); abilities still can. Dense Foliage. */
    public static TargetingRestrictionEffect spells() {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS, false, Set.of(), TargetColorMode.ANY, false);
    }

    /** Can't be the target of spells of the given colors. Karplusan Strider. */
    public static TargetingRestrictionEffect fromSpellColors(Set<CardColor> colors) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS, false, colors, TargetColorMode.BLOCKED_COLORS, false);
    }

    /**
     * Can't be the target of spells of the given colors that an opponent controls; the permanent's
     * own controller may still target it, and abilities of those colors are unaffected.
     * Fiendslayer Paladin.
     */
    public static TargetingRestrictionEffect fromOpponentSpellColors(Set<CardColor> colors) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS, true, colors, TargetColorMode.BLOCKED_COLORS, false);
    }

    /** Hexproof from the given colors — opponents' colored spells/abilities can't target. Knight of Grace/Malice. */
    public static TargetingRestrictionEffect hexproofFromColors(Set<CardColor> colors) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, true, colors, TargetColorMode.BLOCKED_COLORS, true);
    }

    /**
     * Can't be the target of spells of the given colors or abilities from sources of those colors,
     * regardless of who controls them — unlike {@link #hexproofFromColors(Set)}, the permanent's own
     * controller is restricted too. Suq'Ata Firewalker.
     */
    public static TargetingRestrictionEffect fromSourceColors(Set<CardColor> colors) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS_AND_ABILITIES, false, colors, TargetColorMode.BLOCKED_COLORS, false);
    }

    /** Hexproof from the given card types — opponents' spells of those types can't target. */
    public static TargetingRestrictionEffect hexproofFromCardTypes(Set<CardType> cardTypes) {
        return new TargetingRestrictionEffect(
                TargetingSourceKind.SPELLS, true, Set.of(), TargetColorMode.ANY, cardTypes);
    }
}
