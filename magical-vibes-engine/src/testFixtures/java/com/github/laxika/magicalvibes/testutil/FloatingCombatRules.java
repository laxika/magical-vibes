package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesMustAttackTargetPlayerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyChosenCreaturesCanAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SkipChosenStepOrPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Puts the floating combat-structure effects (taunts, chosen attackers, skipped steps, "assigns no combat damage") in
 * force for a test, and reads back what is in force, as the fields they used to be kept in.
 */
public final class FloatingCombatRules {

    private FloatingCombatRules() {
    }

    /** The creatures {@code affectedPlayerId} controls must attack {@code attackTargetId} this turn if able. */
    public static void taunt(GameData gd, UUID controllerId, UUID affectedPlayerId, UUID attackTargetId) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, controllerId, tauntEffect(
                affectedPlayerId, attackTargetId), affectedPlayerId, Duration.UNTIL_END_OF_TURN));
    }

    /** The same, during {@code affectedPlayerId}'s next turn. */
    public static void tauntNextTurn(GameData gd, UUID controllerId, UUID affectedPlayerId, UUID attackTargetId) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, controllerId, tauntEffect(
                affectedPlayerId, attackTargetId), affectedPlayerId, Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
    }

    private static MatchingCreaturesMustAttackEffect tauntEffect(UUID affectedPlayerId, UUID attackTargetId) {
        return new MatchingCreaturesMustAttackEffect(new PermanentControlledByPlayerPredicate(affectedPlayerId),
                attackTargetId);
    }

    /** What the taunts in force on {@code affectedPlayerId} make its creatures attack. */
    public static List<UUID> tauntTargets(GameData gd, UUID affectedPlayerId) {
        return gd.floatingEffectsOnPlayer(affectedPlayerId, MatchingCreaturesMustAttackEffect.class).stream()
                .map(effect -> ((MatchingCreaturesMustAttackEffect) effect.effect()).requiredAttackTargetId())
                .toList();
    }

    /** What the taunts waiting for the affected player's next turn make its creatures attack. */
    public static List<UUID> scheduledTauntTargets(GameData gd, UUID affectedPlayerId) {
        return gd.scheduledFloatingEffects.stream()
                .filter(effect -> affectedPlayerId.equals(effect.affectedPlayerId())
                        && effect.effect() instanceof MatchingCreaturesMustAttackEffect)
                .map(effect -> ((MatchingCreaturesMustAttackEffect) effect.effect()).requiredAttackTargetId())
                .toList();
    }

    /** What the requirements waiting for the controller's next turn make {@code creatureId} attack. */
    public static List<UUID> scheduledRequiredTargets(GameData gd, UUID creatureId) {
        return gd.scheduledFloatingEffects.stream()
                .filter(effect -> creatureId.equals(effect.affectedPermanentId())
                        && effect.effect() instanceof CreaturesMustAttackTargetPlayerUntilNextTurnEffect)
                .map(effect -> ((CreaturesMustAttackTargetPlayerUntilNextTurnEffect) effect.effect()).targetPlayerId())
                .toList();
    }

    /** Only {@code chosen} can attack this turn, and they attack if able. */
    public static void chosenAttackers(GameData gd, UUID controllerId, UUID playerId, Set<UUID> chosen) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, controllerId,
                new OnlyChosenCreaturesCanAttackEffect(chosen), playerId, Duration.UNTIL_END_OF_TURN));
    }

    /** The sets of creatures the chosen-attackers effects in force allow to attack. */
    public static List<Set<UUID>> chosenAttackers(GameData gd, UUID playerId) {
        return gd.floatingEffectsOnPlayer(playerId, OnlyChosenCreaturesCanAttackEffect.class).stream()
                .map(effect -> ((OnlyChosenCreaturesCanAttackEffect) effect.effect()).chosen())
                .toList();
    }

    /** The sets of creatures the chosen-attackers effects waiting for the player's next turn allow to attack. */
    public static List<Set<UUID>> scheduledChosenAttackers(GameData gd, UUID playerId) {
        return gd.scheduledFloatingEffects.stream()
                .filter(effect -> playerId.equals(effect.affectedPlayerId())
                        && effect.effect() instanceof OnlyChosenCreaturesCanAttackEffect)
                .map(effect -> ((OnlyChosenCreaturesCanAttackEffect) effect.effect()).chosen())
                .toList();
    }

    /** {@code playerId} skips every {@code kind} step or phase this turn. */
    public static void skipThisTurn(GameData gd, UUID playerId, SkipStepOrPhaseKind kind) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, playerId,
                new SkipChosenStepOrPhaseEffect(kind), playerId, Duration.UNTIL_END_OF_TURN));
    }

    /** Whether a skip of every {@code kind} step or phase waits for {@code playerId}'s next turn. */
    public static boolean skipsNextTurn(GameData gd, UUID playerId, SkipStepOrPhaseKind kind) {
        return gd.scheduledFloatingEffects.stream().anyMatch(effect -> playerId.equals(effect.affectedPlayerId())
                && effect.effect() instanceof SkipChosenStepOrPhaseEffect skip && skip.kind() == kind);
    }

    /** {@code creatureId} assigns no combat damage for {@code duration}. */
    public static void assignNoCombatDamage(GameData gd, UUID creatureId, Duration duration) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, null,
                new AssignNoCombatDamageEffect(), creatureId, duration));
    }

    /** The queued skips of the next combat phase of {@code playerId}: the counted ones and the ones for this turn. */
    public static int queuedCombatPhaseSkips(GameData gd, UUID playerId) {
        return gd.skipNextCombatPhaseCount.getOrDefault(playerId, 0)
                + gd.floatingEffectsOnPlayer(playerId, SkipNextEffect.class).size();
    }
}
