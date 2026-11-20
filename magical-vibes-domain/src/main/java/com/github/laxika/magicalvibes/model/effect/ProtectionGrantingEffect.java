package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaValueParity;

import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

/**
 * Capability interface for static effects that grant "protection from …". Lets the battlefield
 * query layer ask "what does this effect protect against" — colors, card types, subtypes, or a
 * source mana-value threshold — without knowing the concrete protection record, mirroring how
 * {@link DamageDealingEffect} abstracts damage.
 *
 * <p>Descriptive only: every method states a fact drawn from the record's existing components,
 * never a score and never behaviour. A record fills in only the facet it carries and inherits the
 * empty/self defaults for the rest, so a single {@code instanceof ProtectionGrantingEffect} match
 * replaces the four per-record checks in {@code GameQueryService}.
 *
 * <p>Scope note: this covers the printed, statically-known protection shapes
 * ({@code ProtectionFromColorsEffect}, {@code ProtectionFromCardTypesEffect},
 * {@code ProtectionFromSubtypesEffect}, {@code ProtectionFromManaValueEffect},
 * {@code ProtectionFromManaValueAtMostEffect},
 * {@code ProtectionFromMulticoloredEffect}, {@code ProtectionFromMonocoloredEffect},
 * {@code ProtectionFromEnemyColoredMulticoloredEffect},
 * {@code ProtectionFromAllOtherManaValuesEffect}, {@code ProtectionFromColorlessEffect},
 * {@code ProtectionFromPlayersEffect}).
 * Protection whose
 * protected set is only known at runtime from game state — a chosen color
 * ({@code ProtectionFromChosenColorEffect}, resolved via {@link ChooseColorEffect}) or a chosen player
 * ({@code ProtectionFromChosenPlayerEffect}) — is not a pure function of an effect record's components and
 * deliberately does not implement this interface.
 */
public interface ProtectionGrantingEffect extends CardEffect {

    /** The colors this effect protects against (empty when it is not colour-based protection). */
    default Set<CardColor> protectionFromColors() {
        return Set.of();
    }

    /** Whether this effect protects against sources with two or more colors. */
    default boolean protectionFromMulticolored() {
        return false;
    }

    /** Whether this effect protects against sources with exactly two enemy colors. */
    default boolean protectionFromEnemyColoredMulticolored() {
        return false;
    }

    /** Whether this effect protects against modified creature permanents. */
    default boolean protectionFromModifiedCreatures() {
        return false;
    }

    /** Counter types whose bearer permanents this effect protects against. */
    default Set<CounterType> protectionFromPermanentsWithCounters() {
        return Set.of();
    }

    /** Whether this effect protects against sources with exactly one color. */
    default boolean protectionFromMonocolored() {
        return false;
    }

    /** Whether this effect protects only from spells that have one or more colors. */
    default boolean protectionFromColoredSpells() {
        return false;
    }

    /**
     * For colour-based protection, whether the raw effect applies to the permanent that carries it
     * ({@code null} = self, e.g. Black Knight) or to an equipped creature
     * ({@link Subjects#attachedCreature()}, e.g. Sword of War and Peace). Non-colour protection
     * shapes are always self-scoped and inherit the {@code null} default.
     */
    default PermanentSubject protectionSubject() {
        return null;
    }

    /** The card types this effect protects against (empty when it is not card-type-based). */
    default Set<CardType> protectionFromCardTypes() {
        return Set.of();
    }

    /** The subtypes this effect protects against (empty when it is not subtype-based). */
    default Set<CardSubtype> protectionFromSubtypes() {
        return Set.of();
    }

    /**
     * Whether {@link #protectionFromSubtypes()} applies only when the source is also a creature —
     * "protection from [subtype] creatures" (Riders of Gavony) as opposed to the printed
     * "protection from [subtype]" shape (Baneslayer Angel), which covers every source carrying the
     * subtype including a noncreature Tribal spell.
     */
    default boolean subtypeProtectionRequiresCreatureSource() {
        return false;
    }

    /**
     * The inclusive lower bound of protected source mana values, when this effect grants
     * "protection from mana value N or greater" (e.g. Mistmeadow Skulk); empty otherwise.
     */
    default OptionalInt protectionFromManaValueAtLeast() {
        return OptionalInt.empty();
    }

    /**
     * The inclusive upper bound of protected source mana values, when this effect grants
     * "protection from mana value N or less".
     */
    default OptionalInt protectionFromManaValueAtMost() {
        return OptionalInt.empty();
    }

    /** Whether this effect protects from source mana values matching the source permanent's chosen odd/even quality. */
    default boolean protectionFromManaValueParity() {
        return false;
    }

    /**
     * For {@link #protectionFromManaValueParity()}: the odd/even quality when the effect fixes it, or {@code null}
     * when it is the quality chosen for the permanent that has the ability.
     */
    default ManaValueParity protectionFromFixedManaValueParity() {
        return null;
    }

    /**
     * The possible chosen numbers for protection from every other mana value. An empty set means
     * this effect does not have this protection shape.
     */
    default Set<Integer> protectionFromManaValuesOtherThanChosen() {
        return Set.of();
    }

    /**
     * For {@link #protectionFromManaValuesOtherThanChosen()}: the number when the effect fixes it, or {@code null}
     * when it is the number chosen for the permanent that has the ability.
     */
    default Integer protectionFromManaValuesOtherThanFixed() {
        return null;
    }

    /**
     * Whether this effect grants "protection from everything" (e.g. Progenitus) — protection from
     * every source, regardless of its colour, card type, subtype or mana value. {@code false} for
     * every narrower protection shape. Because every source is an object with at least one card
     * type, {@code GameQueryService.hasProtectionFromSourceCardTypes()} treats this as protection
     * from all sources, which is the shared gate for damage, combat, targeting and enchant/equip.
     */
    default boolean protectsFromEverything() {
        return false;
    }

    /** Whether this effect protects from permanents currently designated as Ring-bearers. */
    default boolean protectionFromRingBearers() {
        return false;
    }

    /** Whether this effect protects from colorless sources (sources with no color). */
    default boolean protectionFromColorless() {
        return false;
    }

    /** The players this effect protects from: every source they control (empty when it is not player-based). */
    default Set<UUID> protectionFromPlayers() {
        return Set.of();
    }
}
