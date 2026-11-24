package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static restriction on this creature (CR 508.1c, 509.1b): it can't attack, or block, unless at least
 * {@code otherCount} other creatures matching {@code companion} also do. "Can't attack or block alone" is one other
 * creature of any kind (CR 506.5); Orcish Conscripts' "unless at least two other creatures attack" is
 * {@link #unlessOthersAlsoDo(int)}; Okk's "unless a creature with greater power also attacks" is
 * {@link #unlessGreaterPowerAlsoDoes()}, its power compared as attackers or blockers are declared; Scarred Puma's
 * "can't attack unless a black or green creature also attacks" is {@link #cantAttackUnlessAlsoAttacks}. Read through
 * the creature's active static abilities, so a face-down creature or one that lost its abilities is free of it.
 *
 * @param restrictsBlocking    whether the blocking half of the restriction applies
 * @param restrictsAttacking   whether the attacking half of the restriction applies
 * @param otherCount           how many other attackers or blockers it needs beside it (at least one)
 * @param companion            what those others must be, judged with this creature as the source ("power greater
 *                             than this creature's"); {@code null} for any creature
 * @param companionDescription the companion's words in "unless [a black or green creature] also attacks";
 *                             {@code null} with no companion filter
 */
public record CantAttackOrBlockAloneEffect(boolean restrictsBlocking, boolean restrictsAttacking, int otherCount,
                                           PermanentPredicate companion, String companionDescription)
        implements CardEffect {

    public CantAttackOrBlockAloneEffect {
        if (otherCount < 1) {
            throw new IllegalArgumentException("A companion restriction needs at least one other creature");
        }
        if ((companion == null) != (companionDescription == null)) {
            throw new IllegalArgumentException("A companion filter comes with its description");
        }
    }

    /** "Can't attack or block alone". */
    public CantAttackOrBlockAloneEffect() {
        this(true, true);
    }

    /** "Can't attack alone" when {@code restrictsBlocking} is false. */
    public CantAttackOrBlockAloneEffect(boolean restrictsBlocking) {
        this(restrictsBlocking, true);
    }

    public CantAttackOrBlockAloneEffect(boolean restrictsBlocking, boolean restrictsAttacking) {
        this(restrictsBlocking, restrictsAttacking, 1, null, null);
    }

    /** "Can't attack unless at least N other creatures attack. Can't block unless at least N other creatures block." */
    public static CantAttackOrBlockAloneEffect unlessOthersAlsoDo(int otherCount) {
        return new CantAttackOrBlockAloneEffect(true, true, otherCount, null, null);
    }

    /**
     * "Can't attack unless a creature with greater power also attacks. Can't block unless a creature with greater power
     * also blocks."
     */
    public static CantAttackOrBlockAloneEffect unlessGreaterPowerAlsoDoes() {
        return new CantAttackOrBlockAloneEffect(true, true, 1,
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate()),
                "a creature with greater power");
    }

    /** "Can't attack unless [a creature matching {@code companion}] also attacks". */
    public static CantAttackOrBlockAloneEffect cantAttackUnlessAlsoAttacks(PermanentPredicate companion,
                                                                          String companionDescription) {
        return new CantAttackOrBlockAloneEffect(false, true, 1, companion, companionDescription);
    }

    /** The restriction's words for a refused declaration: "can't attack alone", "can't block unless ...". */
    public String unmetText(boolean attacking) {
        String verb = attacking ? "attack" : "block";
        if (companion != null) {
            return "can't " + verb + " unless " + companionDescription + " also " + verb + "s";
        }
        if (otherCount == 1) {
            return "can't " + verb + " alone";
        }
        return "can't " + verb + " unless at least " + otherCount + " other creatures " + verb;
    }
}
