package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * One-shot: creatures matching {@code matcher} (any controller) attack this turn if able. The predicate is evaluated
 * with the effect's source controller as {@code sourceControllerId}, so "creatures your opponents control attack this
 * turn if able" is this effect with {@code PermanentNotPredicate(PermanentControlledBySourceControllerPredicate())}.
 * "Creatures target player controls attack this turn if able" is {@link #controlledBy}: the players {@code players}
 * names are found as the effect resolves (none: nothing is required), and only creatures they control match.
 *
 * <p>By default the requirement doesn't change characteristics or control, so it modifies the game
 * rules (CR 611.2c): it is registered as a turn-long {@link MatchingCreaturesMustAttackEffect} and also
 * binds creatures that enter or start matching later this turn (Grisly Anglerfish, Goblin Diplomats, Imaginary
 * Threats).
 *
 * @param lockedAtResolution {@code true} when the text names a fixed set of objects ("those
 *                           creatures", "this creature", overloaded "each creature ... gets -2/-0
 *                           and attacks"): only creatures matching when the effect resolves get the
 *                           transient {@code mustAttackThisTurn} flag (Suicidal Charge, Chemister's
 *                           Trick, Kookus)
 * @param players            whose creatures must attack; {@code null} for every player's
 */
public record MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher, boolean lockedAtResolution,
                                                        PlayerSubject players)
        implements CardEffect {

    public MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher, boolean lockedAtResolution) {
        this(matcher, lockedAtResolution, null);
    }

    public MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher) {
        this(matcher, false);
    }

    /** "Creatures [players] control attack this turn if able" (Instigator's and Incite War's {@code targetPlayer()}). */
    public static MatchingCreaturesMustAttackThisTurnEffect controlledBy(PlayerSubject players) {
        return new MatchingCreaturesMustAttackThisTurnEffect(new PermanentIsCreaturePredicate(), false,
                Objects.requireNonNull(players, "players"));
    }

    @Override
    public TargetSpec targetSpec() {
        return players != null && players.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
