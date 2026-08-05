package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What a {@link CardEffect} may legally target, as a composable value: which candidate
 * <em>domain</em> (battlefield / players / graveyards / exile / stack) the target is drawn from,
 * together with the restriction that applies inside that domain.
 *
 * <p>This is the successor to {@link TargetCategory}, whose flat enum squeezed the domain axis
 * through two booleans ({@code includesPermanents()} / {@code includesPlayers()}) and could
 * therefore not tell "a player or <em>any</em> permanent" apart from "any target" (CR 115.4: a
 * creature, player, planeswalker, or battle). Each kind leaf carries the predicate hierarchy that
 * already exists for its domain, so the 130-odd existing predicate records are reused verbatim and
 * every leaf is evaluated by the service that already owns that hierarchy.</p>
 *
 * <h2>Algebra</h2>
 *
 * <p>{@link AnyOf} is the ONLY cross-kind combinator, and there is deliberately no {@code AllOf}
 * and no {@code Not} at this level:</p>
 *
 * <ul>
 *   <li>A kind-mismatched leaf evaluates to {@code false}, so {@code Not(Permanents(isCreature))}
 *       would be {@code true} for every player, graveyard card and spell — negation would stop
 *       being complement-within-domain and silently produce over-permissive targeting.</li>
 *   <li>A conjunction spanning two kinds is unsatisfiable by construction.</li>
 * </ul>
 *
 * <p>Conjunction and negation are sound only <em>within</em> a kind, where
 * {@code PermanentAllOfPredicate} / {@code PermanentNotPredicate} / {@code CardNotPredicate} / …
 * already provide them. Forbidding both here removes the hazard instead of documenting it.</p>
 *
 * <p>{@code AnyOf} holds at most one leaf per {@link Kind}, enforced structurally: its constructor
 * flattens nested {@code AnyOf}s, rejects two leaves of the same kind, and sorts by kind. That
 * makes it isomorphic to a canonical {@code kind -> predicate} map, which is exactly what target
 * enumeration needs — "what is the permanent restriction? what is the player restriction?" is a
 * {@link #leaf(Kind)} lookup, not a search over the game universe. Two restrictions on the same
 * kind must be merged by the caller into one {@code PermanentAnyOfPredicate} (or the card /
 * stack-entry equivalent), which is the sound spelling.</p>
 *
 * <p>Every leaf's inner predicate is non-{@code null}: use {@code PermanentTruePredicate},
 * {@code CardTruePredicate} or {@code StackEntryTruePredicate} for "no restriction". A
 * {@code null} predicate means "matches nothing" to the permanent evaluator and "matches
 * everything" to the card evaluator, and a target restriction must not depend on which convention
 * a reader assumes.</p>
 *
 * @see TargetPredicates for the factory per {@link TargetCategory} constant
 */
public sealed interface TargetPredicate permits TargetPredicate.Leaf, TargetPredicate.AnyOf {

    /** The candidate domain a {@link Leaf} draws from. One leaf per kind, at most. */
    enum Kind {
        PERMANENT,
        PLAYER,
        GRAVEYARD_CARD,
        EXILED_CARD,
        SPELL
    }

    /** A restriction over exactly one {@link Kind}; the only thing {@link AnyOf} may contain. */
    sealed interface Leaf extends TargetPredicate
            permits Permanents, Players, GraveyardCards, ExiledCards, Spells {

        /** The candidate domain this leaf draws from. */
        Kind kind();
    }

    /** A battlefield permanent matching {@code inner}. */
    record Permanents(PermanentPredicate inner) implements Leaf {

        public Permanents {
            Objects.requireNonNull(inner, "inner (use PermanentTruePredicate for any permanent)");
        }

        @Override
        public Kind kind() {
            return Kind.PERMANENT;
        }
    }

    /** A player matching {@code inner}. */
    record Players(PlayerPredicate inner) implements Leaf {

        public Players {
            Objects.requireNonNull(inner, "inner (use PlayerRelationPredicate(ANY) for any player)");
        }

        @Override
        public Kind kind() {
            return Kind.PLAYER;
        }
    }

    /** A card in a graveyard within {@code scope} matching {@code inner}. */
    record GraveyardCards(CardPredicate inner, GraveyardSearchScope scope) implements Leaf {

        public GraveyardCards {
            Objects.requireNonNull(inner, "inner (use CardTruePredicate for any card)");
            Objects.requireNonNull(scope, "scope");
        }

        @Override
        public Kind kind() {
            return Kind.GRAVEYARD_CARD;
        }
    }

    /** A card in exile matching {@code inner}. */
    record ExiledCards(CardPredicate inner) implements Leaf {

        public ExiledCards {
            Objects.requireNonNull(inner, "inner (use CardTruePredicate for any card)");
        }

        @Override
        public Kind kind() {
            return Kind.EXILED_CARD;
        }
    }

    /** A spell or ability on the stack matching {@code inner}. */
    record Spells(StackEntryPredicate inner) implements Leaf {

        public Spells {
            Objects.requireNonNull(inner, "inner (use StackEntryTruePredicate for any spell)");
        }

        @Override
        public Kind kind() {
            return Kind.SPELL;
        }
    }

    /**
     * Disjunction across kinds — the only cross-kind combinator. Holds at least two leaves, at
     * most one per {@link Kind}, in {@code Kind} order; nested {@code AnyOf}s are flattened away.
     * The canonical ordering makes {@code equals()} insensitive to how the options were written.
     */
    record AnyOf(List<TargetPredicate> options) implements TargetPredicate {

        public AnyOf(TargetPredicate... options) {
            this(List.of(options));
        }

        public AnyOf {
            Objects.requireNonNull(options, "options");
            List<Leaf> flattened = new ArrayList<>();
            flatten(options, flattened);
            if (flattened.size() < 2) {
                throw new IllegalArgumentException(
                        "AnyOf is a cross-kind disjunction and needs at least two leaves; "
                                + "use the leaf itself for a single kind");
            }
            EnumSet<Kind> seen = EnumSet.noneOf(Kind.class);
            for (Leaf leaf : flattened) {
                if (!seen.add(leaf.kind())) {
                    throw new IllegalArgumentException(
                            "AnyOf holds at most one leaf per kind but got two of " + leaf.kind()
                                    + "; merge them into one predicate of that kind "
                                    + "(e.g. PermanentAnyOfPredicate) instead");
                }
            }
            flattened.sort(Comparator.comparing(Leaf::kind));
            options = List.copyOf(flattened);
        }

        private static void flatten(List<TargetPredicate> source, List<Leaf> out) {
            for (TargetPredicate option : source) {
                Objects.requireNonNull(option, "AnyOf option");
                switch (option) {
                    case AnyOf nested -> flatten(nested.options(), out);
                    case Leaf leaf -> out.add(leaf);
                }
            }
        }
    }

    /**
     * The leaf restricting {@code kind}, or empty when this predicate admits no target of that
     * kind. Enumeration reads this to decide which collections to iterate; because {@link AnyOf}
     * is flattened and kind-unique, it is a lookup over at most five entries.
     */
    default Optional<Leaf> leaf(Kind kind) {
        return switch (this) {
            case Leaf self -> self.kind() == kind ? Optional.of(self) : Optional.empty();
            case AnyOf anyOf -> anyOf.options().stream()
                    .map(Leaf.class::cast)
                    .filter(leaf -> leaf.kind() == kind)
                    .findFirst();
        };
    }

    /** Whether a target of {@code kind} can ever be legal for this predicate. */
    default boolean admits(Kind kind) {
        return leaf(kind).isPresent();
    }

    /**
     * The restriction this predicate places on a battlefield permanent, or empty when it admits no
     * permanent at all. Spelled out rather than left as a {@link #leaf(Kind)} lookup plus a cast
     * because it is the question both the spec interpreter and target enumeration ask.
     */
    default Optional<PermanentPredicate> permanentRestriction() {
        return leaf(Kind.PERMANENT).map(leaf -> ((Permanents) leaf).inner());
    }

    /**
     * Which graveyards a card target is drawn from, or empty when this predicate admits no
     * graveyard card at all. This is the single source of truth for graveyard scope: the three
     * mutually-exclusive zone states the old {@code TargetCategory} spelled as three constants are
     * one {@link GraveyardSearchScope} component here, so no reader hand-copies the mapping.
     */
    default Optional<GraveyardSearchScope> graveyardScope() {
        return leaf(Kind.GRAVEYARD_CARD).map(leaf -> ((GraveyardCards) leaf).scope());
    }
}
