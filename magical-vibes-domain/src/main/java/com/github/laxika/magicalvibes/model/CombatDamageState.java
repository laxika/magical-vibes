package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLookAtHandAndDraw;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.*;

/**
 * Mutable state object that tracks all combat damage across both phases (first strike and regular).
 * It lives in the domain model because a card can put the completed damage assignment on the stack,
 * where the state must survive until the stack entry resolves.
 */
public class CombatDamageState {

    /** Redirect shields present at the start of this simultaneous damage event. */
    public List<SourcePermanentAndControllerNextDamageRedirectShield> sharedRedirectShields = List.of();
    public List<SourceNextDamageToAnyTargetShield> sourceDamageShields = List.of();

    public int damageToDefendingPlayer;
    public int poisonDamageToDefendingPlayer;
    public int unpreventableDamageToDefendingPlayer;
    public int damageRedirectedToGuard;
    public int infectDamageRedirectedToGuard;
    public boolean deathtouchDamageRedirectedToGuard;

    public final Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
    public final Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());
    public final Map<Integer, Integer> atkDamageTaken = new HashMap<>();
    public final Map<Integer, Integer> defDamageTaken = new HashMap<>();
    public final Map<Integer, Map<UUID, Integer>> atkDamageTakenBySource = new HashMap<>();
    public final Map<Integer, Map<UUID, Integer>> defDamageTakenBySource = new HashMap<>();
    public final Map<Integer, Integer> unpreventableAtkDamageTaken = new HashMap<>();
    public final Map<Integer, Integer> unpreventableDefDamageTaken = new HashMap<>();

    public final Map<UUID, Integer> damageDealtToPermanentsBeforeStep = new HashMap<>();
    public final Map<UUID, Integer> markedDamageBeforeStep = new HashMap<>();
    public final Map<UUID, Integer> toughnessBeforeStep = new HashMap<>();
    public final Map<UUID, Integer> lethalDamageThresholdBeforeStep = new HashMap<>();
    public final Map<UUID, Integer> loyaltyBeforeStep = new HashMap<>();

    public final Set<Integer> deathtouchDamagedAttackerIndices = new HashSet<>();
    public final Set<Integer> deathtouchDamagedDefenderIndices = new HashSet<>();
    public final Map<UUID, Integer> damageToPlaneswalkers = new HashMap<>();

    public final Map<Permanent, Integer> combatDamageDealt = new HashMap<>();
    public final Map<Permanent, Integer> combatDamageDealtToPlayer = new HashMap<>();
    public final Map<Permanent, Integer> combatDamageDealtToPlaneswalker = new HashMap<>();
    public final Map<Permanent, Map<UUID, Integer>> combatDamageAmountsToBattles = new HashMap<>();
    public final Map<Permanent, Map<UUID, Integer>> combatDamageAmountsToPlaneswalkers = new HashMap<>();
    public final Map<Permanent, Set<UUID>> combatDamageRecipientControllers = new HashMap<>();
    public final Map<Permanent, List<UUID>> combatDamageDealtToCreatures = new HashMap<>();
    public final Set<UUID> combatDamageToBlockingCreatureSources = new HashSet<>();
    public final Map<Permanent, UUID> combatDamageDealerControllers = new HashMap<>();
    public final Map<Permanent, List<CardEffect>> selfDealsCombatDamageEffects = new HashMap<>();
    public final Map<Permanent, List<CardEffect>> selfDealsCombatDamageToPlayerOrPlaneswalkerEffects = new HashMap<>();
    public final Map<Permanent, List<CardEffect>> selfDealsCombatDamageToPlayerOrBattleEffects = new HashMap<>();
    public final Map<Permanent, List<CardEffect>> selfDealsDamageEffects = new HashMap<>();
    public final List<StackEntry> enchantedCreatureDealsDamageTriggers = new ArrayList<>();
    public final List<StackEntry> allyCreatureDealsDamageToPlaneswalkerTriggers = new ArrayList<>();
    public final List<DelayedCombatDamageDrawQualification> delayedCombatDamageDrawQualifications = new ArrayList<>();
    public final List<DelayedCombatDamageLookAtHandAndDrawQualification>
            delayedCombatDamageLookAtHandAndDrawQualifications = new ArrayList<>();

    public final Map<Permanent, Map<UUID, Integer>> combatDamageAmountsToCreatures = new HashMap<>();
    public final Map<UUID, UUID> combatDamageTargetControllers = new HashMap<>();
    public final Map<UUID, Permanent> pendingDralnuReplacementTargets = new LinkedHashMap<>();
    public final Map<UUID, Integer> pendingDralnuReplacementDamage = new LinkedHashMap<>();
    public boolean defenderDamageAsInfect;

    public record DelayedCombatDamageDrawQualification(DelayedCombatDamageDraw delayedAction,
                                                       Set<UUID> sourceIds) {
    }

    public record DelayedCombatDamageLookAtHandAndDrawQualification(
            DelayedCombatDamageLookAtHandAndDraw delayedAction,
            UUID damagedPlayerId) {
    }
}
