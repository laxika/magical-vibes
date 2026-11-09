package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.Zone;
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
 * @param castFrom           the zone the spell must have been cast from ("a spell from your graveyard"), or
 *                           {@code null} for any; the spell counts above count only the spells cast from it
 * @param notCastFrom        a zone the spell must not have been cast from ("a spell from anywhere other than your
 *                           hand"), or {@code null} for none
 * @param kicker             whether the spell must have been kicked, and how often the ability then triggers
 * @param everyPlayersSpells whether the spell counts above count the spells every player cast this turn ("the first
 *                           noncreature spell of a turn") rather than the casting player's
 * @param grantedToTheSpell  whether the ability is one the source gives the spell ("spells you cast with mana value 6
 *                           or greater have cascade", CR 113.1a): the spell is its source (CR 113.7), each of its
 *                           effects is an instance of its own that triggers separately (CR 702.85c), and nothing that
 *                           makes the source permanent's abilities trigger an additional time applies to it
 */
public record SpellFilter(CardPredicate spell, StackEntryPredicate stackEntry, Turn turn, boolean duringCombat,
                          int nthThisTurn, int nthOrLaterThisTurn, boolean paidWithManaFromThis,
                          boolean paidWithTreasureMana, boolean twoOrMoreCardTypes, int expend, Zone castFrom,
                          Zone notCastFrom, Kicker kicker, boolean everyPlayersSpells, boolean grantedToTheSpell) {

    /** Any spell, cast at any time. */
    public static final SpellFilter ANY = new SpellFilter(null, null, Turn.ANY, false, 0, 0, false, false, false, 0,
            null, null, Kicker.ANY, false, false);

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

    /** Whether the spell must have been kicked (CR 702.33d), and how often the ability then triggers. */
    public enum Kicker {
        /** Kicked or not. */
        ANY,
        /** A kicked spell, once however many times it was kicked: "whenever you cast a kicked spell". */
        KICKED,
        /** A kicked spell, once for each time it was kicked: "whenever a player kicks a spell". */
        EACH_KICK
    }

    public SpellFilter {
        Objects.requireNonNull(turn, "turn");
        Objects.requireNonNull(kicker, "kicker");
        if (nthThisTurn < 0 || nthOrLaterThisTurn < 0 || expend < 0) {
            throw new IllegalArgumentException("Spell counts and the expend threshold can't be negative");
        }
        if (castFrom != null && notCastFrom != null) {
            throw new IllegalArgumentException("A spell filter names the zone the spell is cast from or one it isn't");
        }
    }

    /**
     * Whether the filter says only what the spell and its stack object must be: all a copy trigger
     * ({@link SpellTrigger.SpellEvent#COPIED}) can say, since a copy isn't cast.
     */
    public boolean spellAndStackEntryOnly() {
        return equals(new SpellFilter(spell, stackEntry, Turn.ANY, false, 0, 0, false, false, false, 0, null, null,
                Kicker.ANY, false, false));
    }

    public SpellFilter withSpell(CardPredicate spell) {
        return new SpellFilter(Objects.requireNonNull(spell, "spell"), stackEntry, turn, duringCombat, nthThisTurn,
                nthOrLaterThisTurn, paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom,
                notCastFrom, kicker, everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withStackEntry(StackEntryPredicate stackEntry) {
        return new SpellFilter(spell, Objects.requireNonNull(stackEntry, "stackEntry"), turn, duringCombat,
                nthThisTurn, nthOrLaterThisTurn, paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes,
                expend, castFrom, notCastFrom, kicker, everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withTurn(Turn turn) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withDuringCombat() {
        return new SpellFilter(spell, stackEntry, turn, true, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withNthThisTurn(int nth) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, positive(nth), nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withNthOrLaterThisTurn(int nth) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, positive(nth),
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withPaidWithManaFromThis() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn, true,
                paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withPaidWithTreasureMana() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, true, twoOrMoreCardTypes, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withTwoOrMoreCardTypes() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, true, expend, castFrom, notCastFrom, kicker,
                everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withExpend(int threshold) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, positive(threshold), castFrom,
                notCastFrom, kicker, everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withCastFrom(Zone zone) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend,
                Objects.requireNonNull(zone, "zone"), notCastFrom, kicker, everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withNotCastFrom(Zone zone) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom,
                Objects.requireNonNull(zone, "zone"), kicker, everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withKicker(Kicker kicker) {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom,
                kicker, everyPlayersSpells, grantedToTheSpell);
    }

    public SpellFilter withEveryPlayersSpells() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom,
                kicker, true, grantedToTheSpell);
    }

    public SpellFilter withGrantedToTheSpell() {
        return new SpellFilter(spell, stackEntry, turn, duringCombat, nthThisTurn, nthOrLaterThisTurn,
                paidWithManaFromThis, paidWithTreasureMana, twoOrMoreCardTypes, expend, castFrom, notCastFrom,
                kicker, everyPlayersSpells, true);
    }

    private static int positive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Expected a positive number, got " + value);
        }
        return value;
    }
}
