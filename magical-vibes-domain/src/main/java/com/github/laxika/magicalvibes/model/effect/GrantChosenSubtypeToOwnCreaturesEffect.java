package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: the permanents selected by {@code subject} gain the chosen creature type, replacing
 * their other creature types when {@code overriding} is true (Conspiracy). Reads the chosen
 * subtype from the source permanent's {@code chosenSubtype} field.
 * Used by Xenograft, Arcane Adaptation ({@link Subjects#otherCreaturesYouControl()}) and Adaptive Automaton
 * ({@link Subjects#source()} — "this creature is the chosen type in addition to its other types").
 *
 * @param affectsAllZones when {@code true}, also applies to creature spells you control on the
 *                        stack and creature cards you own that aren't on the battlefield (hand,
 *                        graveyard, library, exile). Used by Arcane Adaptation.
 * @param subject           which permanents receive the subtype; only {@link Subjects#otherCreaturesYouControl()},
 *                        {@link Subjects#creaturesYouControl()} and {@link Subjects#source()} are
 *                        supported.
 * @param filter          optional predicate restricting the affected creatures.
 * @param overriding      replaces existing creature types instead of adding to them.
 */
public record GrantChosenSubtypeToOwnCreaturesEffect(boolean affectsAllZones,
                                                     PermanentSubject subject,
                                                     PermanentPredicate filter,
                                                     boolean overriding) implements CardEffect {

    public GrantChosenSubtypeToOwnCreaturesEffect(boolean affectsAllZones, PermanentSubject subject,
                                                  PermanentPredicate filter) {
        this(affectsAllZones, subject, filter, false);
    }

    public static GrantChosenSubtypeToOwnCreaturesEffect replacingInAllZones() {
        return new GrantChosenSubtypeToOwnCreaturesEffect(true, Subjects.otherCreaturesYouControl(), null, true);
    }

    /**
     * Battlefield-only variant granting to each creature you control, including the source when it
     * is a creature (e.g. Xenograft animated by Opalescence).
     */
    public GrantChosenSubtypeToOwnCreaturesEffect() {
        this(false, Subjects.creaturesYouControl(), null);
    }

    public GrantChosenSubtypeToOwnCreaturesEffect(boolean affectsAllZones) {
        this(affectsAllZones, Subjects.otherCreaturesYouControl(), null);
    }

    /** "This creature is the chosen type in addition to its other types." */
    public static GrantChosenSubtypeToOwnCreaturesEffect toSelf() {
        return new GrantChosenSubtypeToOwnCreaturesEffect(false, Subjects.source(), null);
    }

    /** A filtered variant of "this permanent is the chosen type in addition to its other types." */
    public static GrantChosenSubtypeToOwnCreaturesEffect toSelfMatching(PermanentPredicate filter) {
        return new GrantChosenSubtypeToOwnCreaturesEffect(false, Subjects.source(), filter);
    }

    /** "Creatures you control matching the filter are the chosen type in addition to their other types." */
    public static GrantChosenSubtypeToOwnCreaturesEffect ownCreaturesMatching(PermanentPredicate filter) {
        return new GrantChosenSubtypeToOwnCreaturesEffect(false, Subjects.otherCreaturesYouControl(), filter);
    }
}
