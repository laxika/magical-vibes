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
 * @param colors      the colours to protect from, for {@link ColorSet#FIXED}; empty for the other sets
 * @param subject     {@code null} means the permanent itself has protection (e.g. Black Knight), and keeps it as its
 *                    own while it is attached; {@link Subjects#attachedCreature()} means the enchanted or equipped
 *                    creature has it (e.g. Sword of War and Peace)
 * @param auraRemoval which Auras the protection doesn't remove (CR 702.16c): an Aura with a quality its creature has
 *                    protection from is put into its owner's graveyard unless the ability says otherwise
 * @param colorSet    which colours the ability names
 */
public record ProtectionFromColorsEffect(Set<CardColor> colors, PermanentSubject subject,
                                         AuraRemoval auraRemoval, ColorSet colorSet)
        implements ProtectionGrantingEffect {

    /** Which Auras an Aura's "enchanted creature has protection from [colours]" leaves attached. */
    public enum AuraRemoval {
        /**
         * No exception: an Aura with a quality the creature has protection from falls off, this one included (Mask of
         * Law and Grace made to grant protection from white puts itself into its owner's graveyard).
         */
        REMOVES,
        /** "This effect doesn't remove this Aura" (the colour Wards, Ward of Lights, Pledge of Loyalty). */
        KEEPS_THIS_AURA,
        /** "This effect doesn't remove Auras" (Spectra Ward): no Aura attached to the creature falls off by it. */
        KEEPS_AURAS
    }

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
        Objects.requireNonNull(auraRemoval, "auraRemoval");
        Objects.requireNonNull(colorSet, "colorSet");
        colors = colorSet == ColorSet.FIXED ? colors : Set.of();
    }

    public ProtectionFromColorsEffect(Set<CardColor> colors, PermanentSubject subject, AuraRemoval auraRemoval) {
        this(colors, subject, auraRemoval, ColorSet.FIXED);
    }

    public ProtectionFromColorsEffect(Set<CardColor> colors, PermanentSubject subject) {
        this(colors, subject, AuraRemoval.REMOVES, ColorSet.FIXED);
    }

    /**
     * Convenience constructor for creatures that themselves have protection (subject = null / self).
     */
    public ProtectionFromColorsEffect(Set<CardColor> colors) {
        this(colors, null, AuraRemoval.REMOVES, ColorSet.FIXED);
    }

    /** "This creature has protection from the chosen color" (Voice of All). */
    public static ProtectionFromColorsEffect chosenColor() {
        return chosenColor(null);
    }

    /** "[Subject] has protection from the chosen color" ({@code null}: this permanent). */
    public static ProtectionFromColorsEffect chosenColor(PermanentSubject subject) {
        return new ProtectionFromColorsEffect(Set.of(), subject, AuraRemoval.REMOVES, ColorSet.CHOSEN);
    }

    /** "This creature has protection from each color among permanents you control" (Empty-Shrine Kannushi). */
    public static ProtectionFromColorsEffect amongPermanentsYouControl() {
        return amongPermanentsYouControl(null);
    }

    /** "[Subject] has protection from each color among permanents you control" ({@code null}: this permanent). */
    public static ProtectionFromColorsEffect amongPermanentsYouControl(PermanentSubject subject) {
        return new ProtectionFromColorsEffect(Set.of(), subject, AuraRemoval.REMOVES, ColorSet.AMONG_PERMANENTS_YOU_CONTROL);
    }

    /** "[Subject] has protection from each color that's not in your commander's color identity". */
    public static ProtectionFromColorsEffect outsideCommanderColorIdentity(PermanentSubject subject) {
        return new ProtectionFromColorsEffect(Set.of(), subject, AuraRemoval.REMOVES, ColorSet.OUTSIDE_COMMANDER_IDENTITY);
    }

    /** This ability with other fixed colours (a text change rewriting a colour word), everything else kept. */
    public ProtectionFromColorsEffect withColors(Set<CardColor> newColors) {
        return new ProtectionFromColorsEffect(newColors, subject, auraRemoval, colorSet);
    }

    /** This ability with an Aura-removal exception ("This effect doesn't remove this Aura"), everything else kept. */
    public ProtectionFromColorsEffect withAuraRemoval(AuraRemoval newAuraRemoval) {
        return new ProtectionFromColorsEffect(colors, subject, newAuraRemoval, colorSet);
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
