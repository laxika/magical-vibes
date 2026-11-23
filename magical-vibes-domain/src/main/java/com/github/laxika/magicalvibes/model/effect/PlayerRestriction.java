package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * What a {@link PlayerRestrictionEffect} stops its players doing: casting spells (every spell, or those of some card
 * types, or noncreature spells), playing lands, or activating abilities that aren't mana abilities. Built through the
 * factories; each reader in the engine answers one of these questions.
 *
 * @param kind       which action is restricted
 * @param spellTypes for {@link Kind#CAST_SPELLS}, the card types of the spells that can't be cast, in the order the card
 *                   names them ("instant or sorcery"); empty for every spell, and for every other kind
 */
public record PlayerRestriction(Kind kind, List<CardType> spellTypes) {

    /** The action a {@link PlayerRestriction} stops. */
    public enum Kind {
        /** "Can't cast spells", or "can't cast [type] spells" when {@link #spellTypes} names types. */
        CAST_SPELLS,
        /**
         * "Can't cast noncreature spells". Not a set of types: an artifact creature spell is a creature spell, so
         * "artifact" would stop a spell this lets through.
         */
        CAST_NONCREATURE_SPELLS,
        /** "Can't play lands". */
        PLAY_LANDS,
        /** "Can't activate abilities that aren't mana abilities" (loyalty abilities included). */
        ACTIVATE_NON_MANA_ABILITIES
    }

    public PlayerRestriction {
        Objects.requireNonNull(kind, "kind");
        spellTypes = List.copyOf(spellTypes);
        if (kind != Kind.CAST_SPELLS && !spellTypes.isEmpty()) {
            throw new IllegalArgumentException("Only a spell restriction names spell types: " + kind);
        }
        if (new HashSet<>(spellTypes).size() != spellTypes.size()) {
            throw new IllegalArgumentException("A spell type is named once: " + spellTypes);
        }
        if (spellTypes.contains(CardType.LAND)) {
            throw new IllegalArgumentException("A land is not a spell; restrict playing lands instead");
        }
    }

    /** "Can't cast spells". */
    public static PlayerRestriction castingSpells() {
        return new PlayerRestriction(Kind.CAST_SPELLS, List.of());
    }

    /** "Can't cast [type] or [type] spells", the types in the order the card names them. */
    public static PlayerRestriction castingSpellsOf(CardType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("Name at least one spell type, or use castingSpells()");
        }
        return new PlayerRestriction(Kind.CAST_SPELLS, List.of(types));
    }

    /** "Can't cast noncreature spells". */
    public static PlayerRestriction castingNoncreatureSpells() {
        return new PlayerRestriction(Kind.CAST_NONCREATURE_SPELLS, List.of());
    }

    /** "Can't play lands". */
    public static PlayerRestriction playingLands() {
        return new PlayerRestriction(Kind.PLAY_LANDS, List.of());
    }

    /** "Can't activate abilities that aren't mana abilities". */
    public static PlayerRestriction activatingNonManaAbilities() {
        return new PlayerRestriction(Kind.ACTIVATE_NON_MANA_ABILITIES, List.of());
    }

    /** Whether this stops every spell. */
    public boolean stopsEverySpell() {
        return kind == Kind.CAST_SPELLS && spellTypes.isEmpty();
    }

    /** The restricted action as a log line says it after "can't": "cast instant or sorcery spells", "play lands". */
    public String description() {
        return switch (kind) {
            case CAST_SPELLS -> spellTypes.isEmpty() ? "cast spells" : "cast " + spellTypes.stream()
                    .map(type -> type.getDisplayName().toLowerCase())
                    .collect(Collectors.joining(" or ")) + " spells";
            case CAST_NONCREATURE_SPELLS -> "cast noncreature spells";
            case PLAY_LANDS -> "play lands";
            case ACTIVATE_NON_MANA_ABILITIES -> "activate abilities that aren't mana abilities";
        };
    }
}
