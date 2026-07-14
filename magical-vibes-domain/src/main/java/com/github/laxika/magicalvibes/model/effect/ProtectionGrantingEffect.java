package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.OptionalInt;
import java.util.Set;

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
 * <p>Scope note: this covers the four printed, statically-known protection shapes
 * ({@code ProtectionFromColorsEffect}, {@code ProtectionFromCardTypesEffect},
 * {@code ProtectionFromSubtypesEffect}, {@code ProtectionFromManaValueEffect}). Protection whose
 * protected set is only known at runtime from game state — a chosen color
 * ({@code ProtectionFromChosenColorEffect}, resolved via {@link ChooseColorEffect}) or a
 * "protection from non-[subtype] creatures" grant tracked on the {@code Permanent} — is not a pure
 * function of an effect record's components and deliberately does not implement this interface.
 */
public interface ProtectionGrantingEffect extends CardEffect {

    /** The colors this effect protects against (empty when it is not colour-based protection). */
    default Set<CardColor> protectionFromColors() {
        return Set.of();
    }

    /**
     * For colour-based protection, whether the raw effect applies to the permanent that carries it
     * ({@code null} = self, e.g. Black Knight) or to an equipped creature
     * ({@link GrantScope#EQUIPPED_CREATURE}, e.g. Sword of War and Peace). Non-colour protection
     * shapes are always self-scoped and inherit the {@code null} default.
     */
    default GrantScope protectionScope() {
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
     * The inclusive lower bound of protected source mana values, when this effect grants
     * "protection from mana value N or greater" (e.g. Mistmeadow Skulk); empty otherwise.
     */
    default OptionalInt protectionFromManaValueAtLeast() {
        return OptionalInt.empty();
    }
}
