package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Objects;
import java.util.Set;

/**
 * Grants protection from colours (CR 702.16a): the colours named, or a colour set judged as it is asked.
 *
 * <p>Which colours a set names depends on the game for every {@link ColorSet} but {@link ColorSet#FIXED}, so the
 * engine reads them through {@code GameQueryService.protectionColors}, judged for the permanent that has the ability
 * (its chosen colour, its controller's permanents and commanders); {@link #protectionFromColors()} is only the fixed
 * colours.
 *
 * @param colors             the colours to protect from, for {@link ColorSet#FIXED}; empty for the other sets
 * @param subject            {@code null} means the permanent itself has protection (e.g. Black Knight);
 *                           {@link Subjects#attachedCreature()} means the enchanted or equipped creature has it (e.g.
 *                           Sword of War and Peace). An Aura that gives its creature protection never makes itself
 *                           illegally attached by it ("This effect doesn't remove this Aura").
 * @param doesNotRemoveAuras {@code true} for "This effect doesn't remove Auras" (Spectra Ward): an
 *                           Aura already attached to the creature with this protection stays attached.
 * @param colorSet           which colours the ability names
 */
public record ProtectionFromColorsEffect(Set<CardColor> colors, PermanentSubject subject,
                                         boolean doesNotRemoveAuras, ColorSet colorSet)
        implements ProtectionGrantingEffect {

    /** The colours a "protection from [colours]" ability names. */
    public enum ColorSet {
        /** The colours printed in the ability ("protection from black and from red"). */
        FIXED(false),
        /** "The chosen color": the colour chosen for the permanent that has the ability (Voice of All, Ward of Lights). */
        CHOSEN(false),
        /** "Each color among permanents you control" (Empty-Shrine Kannushi, Pledge of Loyalty), as they are now. */
        AMONG_PERMANENTS_YOU_CONTROL(true),
        /**
         * "Each color that's not in your commander's color identity" (Commander's Plate, CR 903.4): none while you
         * have no commander, the combined identity of two.
         */
        OUTSIDE_COMMANDER_IDENTITY(true);

        private final boolean readsBoard;

        ColorSet(boolean readsBoard) {
            this.readsBoard = readsBoard;
        }

        /** Whether the colours depend on other objects, so a permanent's own ability is judged in layer 6, not seeded. */
        public boolean readsBoard() {
            return readsBoard;
        }
    }

    public ProtectionFromColorsEffect {
        Objects.requireNonNull(colorSet, "colorSet");
        colors = colorSet == ColorSet.FIXED ? colors : Set.of();
    }

    public ProtectionFromColorsEffect(Set<CardColor> colors, PermanentSubject subject, boolean doesNotRemoveAuras) {
        this(colors, subject, doesNotRemoveAuras, ColorSet.FIXED);
    }

    public ProtectionFromColorsEffect(Set<CardColor> colors, PermanentSubject subject) {
        this(colors, subject, false, ColorSet.FIXED);
    }

    /**
     * Convenience constructor for creatures that themselves have protection (subject = null / self).
     */
    public ProtectionFromColorsEffect(Set<CardColor> colors) {
        this(colors, null, false, ColorSet.FIXED);
    }

    /** "This creature has protection from the chosen color" (Voice of All). */
    public static ProtectionFromColorsEffect chosenColor() {
        return chosenColor(null);
    }

    /** "[Subject] has protection from the chosen color" ({@code null}: this permanent). */
    public static ProtectionFromColorsEffect chosenColor(PermanentSubject subject) {
        return new ProtectionFromColorsEffect(Set.of(), subject, false, ColorSet.CHOSEN);
    }

    /** "This creature has protection from each color among permanents you control" (Empty-Shrine Kannushi). */
    public static ProtectionFromColorsEffect amongPermanentsYouControl() {
        return amongPermanentsYouControl(null);
    }

    /** "[Subject] has protection from each color among permanents you control" ({@code null}: this permanent). */
    public static ProtectionFromColorsEffect amongPermanentsYouControl(PermanentSubject subject) {
        return new ProtectionFromColorsEffect(Set.of(), subject, false, ColorSet.AMONG_PERMANENTS_YOU_CONTROL);
    }

    /** "[Subject] has protection from each color that's not in your commander's color identity". */
    public static ProtectionFromColorsEffect outsideCommanderColorIdentity(PermanentSubject subject) {
        return new ProtectionFromColorsEffect(Set.of(), subject, false, ColorSet.OUTSIDE_COMMANDER_IDENTITY);
    }

    /** This ability with other fixed colours (a text change rewriting a colour word), everything else kept. */
    public ProtectionFromColorsEffect withColors(Set<CardColor> newColors) {
        return new ProtectionFromColorsEffect(newColors, subject, doesNotRemoveAuras, colorSet);
    }

    /** The fixed colours; a colour set judged as asked is read by {@code GameQueryService.protectionColors}. */
    @Override
    public Set<CardColor> protectionFromColors() {
        return colors;
    }

    @Override
    public PermanentSubject protectionSubject() {
        return subject;
    }
}
