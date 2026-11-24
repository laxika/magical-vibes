package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;
import java.util.UUID;

/**
 * Only creatures matching {@code filter} can attack while this floating effect lasts (CR 508.1c); every other creature
 * can't. How long is the floating effect's {@code Duration}: an added combat phase ("only land creatures can attack
 * during that combat", {@code Duration.duringCombatPhase}), the aggressive combat phase ("only creatures with
 * aggressive may attack during it", {@code UNTIL_END_OF_COMBAT}) or a player's next turn (Oracle en-Vec's "other
 * creatures can't attack", {@code DURING_AFFECTED_PLAYERS_NEXT_TURN}, beside a {@link MatchingCreaturesMustAttackEffect}
 * for "the chosen creatures attack if able"). The floating effect may be on every player or on one; it restricts every
 * creature while it applies.
 *
 * <p>The filter is judged against each creature's current characteristics with the floating effect's source permanent
 * and controller as the filter context: {@link #lands()}, {@link #source()} ("only this creature can attack during that
 * combat", Throat Wolf; the added combat is then its controller's), {@link #only(Set)} (the chosen or targeted
 * creatures; a creature that entered after the choice is outside the set, and an empty set bars every creature).
 *
 * @param filter the creatures that can still attack
 */
public record OnlyMatchingCreaturesCanAttackThisCombatEffect(PermanentPredicate filter) implements CardEffect {

    /** "Only land creatures can attack during that combat." */
    public static OnlyMatchingCreaturesCanAttackThisCombatEffect lands() {
        return new OnlyMatchingCreaturesCanAttackThisCombatEffect(new PermanentIsLandPredicate());
    }

    /** "Only [this creature] can attack during that combat." */
    public static OnlyMatchingCreaturesCanAttackThisCombatEffect source() {
        return new OnlyMatchingCreaturesCanAttackThisCombatEffect(new PermanentIsSourcePermanentPredicate());
    }

    /** Only the permanents {@code permanentIds} can attack; none can when it is empty. */
    public static OnlyMatchingCreaturesCanAttackThisCombatEffect only(Set<UUID> permanentIds) {
        return new OnlyMatchingCreaturesCanAttackThisCombatEffect(new PermanentAnyOfPredicate(permanentIds.stream()
                .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList()));
    }

    /** Whether this is {@link #source()}: only its source can attack. */
    public boolean onlySource() {
        return filter instanceof PermanentIsSourcePermanentPredicate;
    }
}
