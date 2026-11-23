package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Conjure a duplicate of [it] onto the battlefield": for each object {@code of} names, a new non-token card that is a
 * duplicate of that object's card enters the battlefield under the controller of the spell or ability, all of them at
 * once ("for each of them, conjure a duplicate of it"). The card is read as a copy of the object would read it, as it
 * is or as it last existed (CR 608.2h): a face-down permanent's duplicate has its face-down values, a spell cast face
 * down is duplicated as effects see it. A token is not a card and has no duplicate.
 *
 * <p>The objects: {@code Subjects.triggeringSpell()} for "that card" after "whenever you cast" (the spell's card),
 * {@code Subjects.triggeringAll()} for the permanents a trigger event was about, {@code Subjects.searched()} for the
 * card a look or search of the same resolution put onto the battlefield. "It gains haste" and "at the beginning of the
 * next end step, shuffle it into its owner's library" are later steps over {@code Subjects.createdThisResolution()}.
 *
 * @param of            the objects whose cards are duplicated; never a target
 * @param basePower     "the duplicate perpetually has base power and toughness N/M": the new card's base power, or
 *                      {@code null} to keep the duplicated value
 * @param baseToughness the new card's base toughness, or {@code null} to keep the duplicated value
 */
public record ConjureDuplicateOntoBattlefieldEffect(PermanentSubject of, Integer basePower, Integer baseToughness)
        implements CardEffect {

    public ConjureDuplicateOntoBattlefieldEffect {
        Objects.requireNonNull(of, "of");
        if (of.declaresTarget()) {
            throw new IllegalArgumentException("A duplicate's original is named, not targeted: " + of);
        }
        if ((basePower == null) != (baseToughness == null)) {
            throw new IllegalArgumentException("Base power and toughness are set together");
        }
    }

    /** "Conjure a duplicate of [it] onto the battlefield." */
    public static ConjureDuplicateOntoBattlefieldEffect of(PermanentSubject of) {
        return new ConjureDuplicateOntoBattlefieldEffect(of, null, null);
    }

    /** This duplicate, which "perpetually has base power and toughness {@code power}/{@code toughness}". */
    public ConjureDuplicateOntoBattlefieldEffect withBasePowerToughness(int power, int toughness) {
        return new ConjureDuplicateOntoBattlefieldEffect(of, power, toughness);
    }
}
