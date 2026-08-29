package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: the permanents selected by {@code scope} are the chosen type in addition to their
 * other types. Reads the chosen subtype from the source permanent's {@code chosenSubtype} field.
 * Used by Xenograft, Arcane Adaptation ({@link GrantScope#OWN_CREATURES}) and Adaptive Automaton
 * ({@link GrantScope#SELF} — "this creature is the chosen type in addition to its other types").
 *
 * @param affectsAllZones when {@code true}, also applies to creature spells you control on the
 *                        stack and creature cards you own that aren't on the battlefield (hand,
 *                        graveyard, library, exile). Used by Arcane Adaptation.
 * @param scope           which permanents receive the subtype; only {@link GrantScope#OWN_CREATURES}
 *                        and {@link GrantScope#SELF} are supported.
 * @param filter          optional predicate restricting the affected creatures.
 */
public record GrantChosenSubtypeToOwnCreaturesEffect(boolean affectsAllZones,
                                                     GrantScope scope,
                                                     PermanentPredicate filter) implements CardEffect {

    /** Battlefield-only variant granting to each creature you control (e.g. Xenograft). */
    public GrantChosenSubtypeToOwnCreaturesEffect() {
        this(false, GrantScope.OWN_CREATURES, null);
    }

    public GrantChosenSubtypeToOwnCreaturesEffect(boolean affectsAllZones) {
        this(affectsAllZones, GrantScope.OWN_CREATURES, null);
    }

    /** "This creature is the chosen type in addition to its other types." */
    public static GrantChosenSubtypeToOwnCreaturesEffect toSelf() {
        return new GrantChosenSubtypeToOwnCreaturesEffect(false, GrantScope.SELF, null);
    }

    /** A filtered variant of "this permanent is the chosen type in addition to its other types." */
    public static GrantChosenSubtypeToOwnCreaturesEffect toSelfMatching(PermanentPredicate filter) {
        return new GrantChosenSubtypeToOwnCreaturesEffect(false, GrantScope.SELF, filter);
    }

    /** "Creatures you control matching the filter are the chosen type in addition to their other types." */
    public static GrantChosenSubtypeToOwnCreaturesEffect ownCreaturesMatching(PermanentPredicate filter) {
        return new GrantChosenSubtypeToOwnCreaturesEffect(false, GrantScope.OWN_CREATURES, filter);
    }
}
