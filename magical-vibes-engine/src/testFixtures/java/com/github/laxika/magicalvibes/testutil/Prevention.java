package com.github.laxika.magicalvibes.testutil;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.PlayerSourceDamagePrevention;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Puts floating damage-prevention effects (CR 615) in force for a test, and reads back whom they apply to, as the
 * sets the effects used to be kept in.
 */
public final class Prevention {

    private Prevention() {
    }

    /** Puts {@code effect} in force on every player and permanent until end of turn. */
    public static void add(GameData gd, PreventDamageEffect effect) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, null, effect, null,
                Duration.UNTIL_END_OF_TURN));
    }

    /** Puts {@code effect} in force on {@code playerId} (its controller) until end of turn. */
    public static void addOnPlayer(GameData gd, PreventDamageEffect effect, UUID playerId) {
        addOnPlayer(gd, effect, playerId, Duration.UNTIL_END_OF_TURN);
    }

    /** Puts {@code effect} in force on {@code playerId} (its controller) for {@code duration}. */
    public static void addOnPlayer(GameData gd, PreventDamageEffect effect, UUID playerId, Duration duration) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, playerId, effect, playerId, duration));
    }

    /** Puts {@code effect} in force on the permanent {@code permanentId} until end of turn. */
    public static void addOnPermanent(GameData gd, PreventDamageEffect effect, UUID permanentId) {
        gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, null, effect, permanentId,
                Duration.UNTIL_END_OF_TURN));
    }

    /** The players the floating prevention effects of any of {@code scopes} name. */
    public static Set<UUID> players(GameData gd, PreventionScope... scopes) {
        Set<UUID> result = new HashSet<>();
        gd.preventionEffects(scopes).forEach(fe -> {
            if (fe.affectedPlayerId() != null) {
                result.add(fe.affectedPlayerId());
            }
        });
        return result;
    }

    /** The permanents the floating prevention effects of any of {@code scopes} with that combat flag name. */
    public static Set<UUID> permanents(GameData gd, boolean combatOnly, PreventionScope... scopes) {
        Set<UUID> result = new HashSet<>();
        gd.preventionEffects(scopes).forEach(fe -> {
            if (fe.affectedPermanentId() != null && ((PreventDamageEffect) fe.effect()).combatOnly() == combatOnly) {
                result.add(fe.affectedPermanentId());
            }
        });
        return result;
    }

    /** The ids of the sources {@code playerId}'s "prevent all damage a source of your choice would deal to you" names. */
    public static Set<UUID> sourceIds(GameData gd, UUID playerId) {
        Set<UUID> result = new HashSet<>();
        gd.floatingPlayerEffects(playerId, PlayerSourceDamagePrevention.class)
                .forEach(prevention -> result.addAll(((PlayerSourceDamagePrevention) prevention).sourceIds()));
        return result;
    }

    /** The players that have a chosen-source prevention in force. */
    public static Set<UUID> playersWithSourcePrevention(GameData gd) {
        return gd.playersWithFloatingEffect(PlayerSourceDamagePrevention.class);
    }

    /** The players the floating effects of {@code type} name, once for each effect, oldest first. */
    public static List<UUID> playersOf(GameData gd, Class<? extends CardEffect> type) {
        return gd.floatingEffectsOfTypes(type).stream().map(FloatingContinuousEffect::affectedPlayerId).toList();
    }

    /** The highest "damage can't reduce your life total below" floor of {@code playerId}'s turn-long effects, or 0. */
    public static int damageLifeFloor(GameData gd, UUID playerId) {
        return gd.floatingPlayerEffects(playerId, DamageLifeFloorUntilEndOfTurnEffect.class).stream()
                .mapToInt(effect -> ((DamageLifeFloorUntilEndOfTurnEffect) effect).floor()).max().orElse(0);
    }

    /** The colors of every floating "prevent all damage from [colors] sources" effect. */
    public static Set<CardColor> sourceColors(GameData gd) {
        Set<CardColor> result = new HashSet<>();
        gd.preventionEffects(PreventionScope.ALL_FROM_COLORS)
                .forEach(fe -> result.addAll(((PreventDamageEffect) fe.effect()).sourceColors()));
        return result;
    }

    /** The players all damage to which (and to their creatures or permanents) is prevented. */
    public static Set<UUID> playersWithAllDamagePrevented(GameData gd) {
        return players(gd, PreventionScope.ALL_TO_CONTROLLER_AND_PERMANENTS,
                PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES);
    }

    /** Permanents prevented from dealing damage this turn. */
    public static Set<UUID> preventedFromDealing(GameData gd) {
        return permanents(gd, false, PreventionScope.ALL_BY_TARGET_CREATURES, PreventionScope.ALL_BY_SELF);
    }

    /** Permanents prevented from dealing combat damage. */
    public static Set<UUID> preventedFromDealingCombat(GameData gd) {
        return permanents(gd, true, PreventionScope.ALL_BY_TARGET_CREATURES, PreventionScope.ALL_BY_SELF);
    }

    /** Permanents all damage to which is prevented this turn. */
    public static Set<UUID> allDamagePrevented(GameData gd) {
        return permanents(gd, false, PreventionScope.ALL_TO_TARGET_CREATURES, PreventionScope.ALL_TO_SELF,
                PreventionScope.ALL_TO_CONTROLLED_MATCHING_PERMANENTS_AT_RESOLUTION);
    }

    /** Permanents all combat damage to which is prevented. */
    public static Set<UUID> combatDamagePrevented(GameData gd) {
        return permanents(gd, true, PreventionScope.ALL_TO_TARGET_CREATURES, PreventionScope.ALL_TO_SELF);
    }
}
