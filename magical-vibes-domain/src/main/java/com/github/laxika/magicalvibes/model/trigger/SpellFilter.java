package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.Objects;

/**
 * What a cast spell must be, and how and when it must have been cast, for a "whenever [a player] casts a [quality]
 * spell" trigger to trigger: the trigger condition of a {@link SpellTrigger} (CR 601.2i). Build it through the
 * {@code SpellTrigger} methods ({@code Triggers.youCastSpell().withFilter(creatureSpell).duringYourTurn()}).
 *
 * @param spell              what the spell must be ("a creature spell", "a blue spell"), judged on the spell as it is
 *                           on the stack, or {@code null} for any spell
 * @param stackEntry         what the spell's stack object must be ("a spell that targets this creature", "a spell
 *                           cast from your graveyard"), or {@code null} for any
 * @param turn               whose turn it must be as the spell is cast
 * @param duringCombat       whether the spell must be cast during combat
 * @param nthThisTurn        if positive, the spell must be the casting player's {@code nthThisTurn}th spell matching
 *                           {@code spell} this turn ("your second spell each turn")
 * @param nthOrLaterThisTurn if positive, the spell must be at least the casting player's {@code nthOrLaterThisTurn}th
 *                           spell matching {@code spell} this turn ("your third spell or later each turn")
 * @param paidWithManaFromThis whether mana the ability's source produced must have been spent to cast the spell ("when
 *                           you spend this mana to cast a spell"); the ability then triggers once for each such mana
 * @param paidWithTreasureMana whether mana a Treasure produced must have been spent to cast the spell
 * @param twoOrMoreCardTypes whether the spell must have two or more card types
 * @param expend             if positive, the spell must be the one that makes its caster spend their
 *                           {@code expend}th total mana on spells this turn ("whenever you expend 4")
 */
public record SpellFilter(CardPredicate spell, StackEntryPredicate stackEntry, Turn turn, boolean duringCombat,
                          int nthThisTurn, int nthOrLaterThisTurn, boolean paidWithManaFromThis,
                          boolean paidWithTreasureMana, boolean twoOrMoreCardTypes, int expend) {

    /** Any spell, cast at any time. */
    public static final SpellFilter ANY = new SpellFilter(null, null, Turn.ANY, false, 0, 0, false, false, false, 0);

    /** Whose turn it must be as the spell is cast. */
    public enum Turn {
        /** Any turn. */
        ANY,
        /** The turn of the ability's controller: "whenever an opponent casts a spell during your turn". */
        YOURS,
        /** A turn other than the ability's controller's: "whenever you cast a spell during an opponent's turn". */
        OPPONENTS,
        /** A turn other than the casting player's: "whenever a player casts a spell, if it isn't that player's turn". */
        NOT_CASTERS
    }

    public SpellFilter {
        Objects.requireNonNull(turn, "turn");
        if (nthThisTurn < 0 || nthOrLaterThisTurn < 0 || expend < 0) {
            throw new IllegalArgumentException("Spell counts and the expend threshold can't be negative");
        }
    }

    public SpellFilter withSpell(CardPredicate spell) {
        return new SpellFilter(Objects.requireNonNull(spell, "spell"), stackEntry, turn, duringCombat, nthThisTurn,
                nthOrLaterThisTurn, paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withStackEntry(StackEntryPredicate stackEntry) {
        return new SpellFilter(spell, Objects.requireNonNull(stackEntry, "stackEntry"), turn, duringCombat,
                nthThisTurn, nthOrLaterThisTurn, paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes,
                expend);
    }

    public SpellFilter withTurn(Turn turn) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withDuringCombat() {
        return new SpellFilter(spell, stackEntry, turn, true, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withNthThisTurn(int nth) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, positive(nth), nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withNthOrLaterThisTurn(int nth) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, positive(nth),
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withPaidWithManaFromThis() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn, true,
                paidWithTreasureMana, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withPaidWithTreasureMana() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, true, twoOrMoreCardTypes, expend);
    }

    public SpellFilter withTwoOrMoreCardTypes() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, true, expend);
    }

    public SpellFilter withExpend(int threshold) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, positive(threshold));
    }

    private static int positive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Expected a positive number, got " + value);
        }
        return value;
    }
}
