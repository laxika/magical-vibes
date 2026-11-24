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
 * rules (CR 611.2c): it is registered as a {@link MatchingCreaturesMustAttackEffect} and also
 * binds creatures that enter or start matching later (Grisly Anglerfish, Goblin Diplomats, Imaginary
 * Threats, Gideon Jura's creatures gained after the ability resolved).
 *
 * <p>"Creatures that player controls attack you / attack Gideon Jura if able" names what they attack
 * ({@link #attacking(PlayerSubject)}, {@link #attacking(PermanentSubject)}), found as the effect resolves; a player
 * named is attacked, not the planeswalkers they control. A creature that can't attack it may attack anything else or
 * not at all, and no attack cost has to be paid to obey (CR 508.1d).
 *
 * @param lockedAtResolution {@code true} when the text names a fixed set of objects ("those
 *                           creatures", "this creature", overloaded "each creature ... gets -2/-0
 *                           and attacks"): only creatures matching when the effect resolves get the
 *                           transient {@code mustAttackThisTurn} flag (Suicidal Charge, Chemister's
 *                           Trick, Kookus)
 * @param players            whose creatures must attack; {@code null} for every player's
 * @param attackedPlayer     the player they must attack ("attack you": Taunt's {@code you()}), or {@code null}
 * @param attackedPermanent  the planeswalker they must attack ("attack Gideon Jura": {@code source()}), or
 *                           {@code null}; at most one of the two is set, neither for any player or planeswalker
 * @param duration           {@link Duration#UNTIL_END_OF_TURN}, or {@link Duration#DURING_AFFECTED_PLAYERS_NEXT_TURN}
 *                           for "during target player's next turn, creatures that player controls attack ... if
 *                           able" (each combat of that turn), which needs {@code players}
 */
public record MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher, boolean lockedAtResolution,
                                                        PlayerSubject players, PlayerSubject attackedPlayer,
                                                        PermanentSubject attackedPermanent, Duration duration)
        implements CardEffect {

    public MatchingCreaturesMustAttackThisTurnEffect {
        Objects.requireNonNull(duration, "duration");
        if (attackedPlayer != null && attackedPermanent != null) {
            throw new IllegalArgumentException("Name one attacked player or permanent, not both");
        }
        if (lockedAtResolution && (attackedPlayer != null || attackedPermanent != null
                || duration != Duration.UNTIL_END_OF_TURN)) {
            throw new IllegalArgumentException("A locked set attacks anything this turn; use SetCombatRequirementThisTurnEffect");
        }
        if (duration != Duration.UNTIL_END_OF_TURN
                && (duration != Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN || players == null)) {
            throw new IllegalArgumentException("Only the named players' next turn is supported: " + duration);
        }
    }

    public MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher, boolean lockedAtResolution,
                                                     PlayerSubject players) {
        this(matcher, lockedAtResolution, players, null, null, Duration.UNTIL_END_OF_TURN);
    }

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

    /** This requirement, attacking the player {@code player} names (Taunt's "attack you"). */
    public MatchingCreaturesMustAttackThisTurnEffect attacking(PlayerSubject player) {
        return new MatchingCreaturesMustAttackThisTurnEffect(matcher, lockedAtResolution, players,
                Objects.requireNonNull(player, "player"), null, duration);
    }

    /** This requirement, attacking the planeswalker {@code permanent} names (Gideon Jura's "attack Gideon Jura"). */
    public MatchingCreaturesMustAttackThisTurnEffect attacking(PermanentSubject permanent) {
        return new MatchingCreaturesMustAttackThisTurnEffect(matcher, lockedAtResolution, players, null,
                Objects.requireNonNull(permanent, "permanent"), duration);
    }

    /** This requirement for {@code newDuration} (the named players' next turn). */
    public MatchingCreaturesMustAttackThisTurnEffect lasting(Duration newDuration) {
        return new MatchingCreaturesMustAttackThisTurnEffect(matcher, lockedAtResolution, players, attackedPlayer,
                attackedPermanent, newDuration);
    }

    @Override
    public TargetSpec targetSpec() {
        return players != null && players.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
