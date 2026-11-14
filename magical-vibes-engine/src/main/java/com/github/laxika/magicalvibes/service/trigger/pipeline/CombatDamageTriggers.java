package com.github.laxika.magicalvibes.service.trigger.pipeline;





import com.github.laxika.magicalvibes.model.trigger.Triggers;

import com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers.Read;
import com.github.laxika.magicalvibes.model.rules.CombatDamageStepEvent;
import com.github.laxika.magicalvibes.model.CombatDamageState;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatDamageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageBecomeMonarch;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageEffect;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLookAtHandAndDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageReflection;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageToken;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDamagedByWatchedCreature;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDealingCombatDamageToPlaneswalker;
import com.github.laxika.magicalvibes.model.action.DelayedNamedCreatureCombatDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreaturesCombatDamage;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Triggers the abilities a combat damage step triggers (CR 510.2, 603.2): the damage and combat-damage abilities of the
 * permanents, graveyard cards, planes and emblems that see the damage, and the delayed abilities watching it (CR 603.7),
 * each handed whole to the stack door with the record of the damage.
 */
@Slf4j
@Service
public class CombatDamageTriggers {

    private static final PermanentHasSameNameAsSourcePredicate SAME_NAME_AS_SOURCE =
            new PermanentHasSameNameAsSourcePredicate();

    @Autowired @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;
    @Autowired @Lazy
    private GameQueryService gameQueryService;
    @Autowired @Lazy
    private PredicateEvaluationService predicateEvaluationService;
    @Autowired @Lazy
    private ConditionEvaluationService conditionEvaluationService;
    @Autowired @Lazy
    private GameLogService gameLogService;
    @Autowired @Lazy
    private GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    @Autowired @Lazy
    private TriggerCollectionService triggerCollectionService;
    @Autowired @Lazy
    private DamageTriggers damageTriggers;
    @Autowired @Lazy
    private TriggerIndex triggerIndex;

    /**
     * One triggered ability as its collector puts it on the stack: the ability and its effects in order, the effects of
     * one stack object (CR 603.3).
     */
    /**
     * {@code abilities}, for a collector of the combat condition watched as {@code watched} that reads no filter,
     * limit or multiplicity: none of its abilities is written with one, and this throws on one.
     */
    private static List<TriggeredAbility> plainCombatAbilities(List<TriggeredAbility> abilities, Trigger watched) {
        for (TriggeredAbility ability : abilities) {
            if (Read.filter(ability, watched) != null
                    || ability.options().limit() != TriggerLimit.NONE
                    || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                throw new IllegalStateException("The collector of " + watched + " reads no filter, limit or "
                        + "multiplicity: " + ability);
            }
        }
        return abilities;
    }

    /**
     * {@code abilities}, for a collector of the damage condition watched as {@code watched} that reads no filter,
     * limit or multiplicity: none of its abilities is written with one, and this throws on one.
     */
    private static List<TriggeredAbility> plainDamageAbilities(List<TriggeredAbility> abilities, Trigger watched) {
        for (TriggeredAbility ability : abilities) {
            if (Read.filter(ability, watched) != null
                    || ability.options().limit() != TriggerLimit.NONE
                    || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                throw new IllegalStateException("The collector of " + watched + " reads no filter, limit or "
                        + "multiplicity: " + ability);
            }
        }
        return abilities;
    }

    /**
     * {@code abilities} read for the combat condition watched as {@code watched}, one list for each ability
     * (CR 603.3), for a collector that reads no filter, limit or multiplicity: none of its abilities is written with
     * one, and this throws on one.
     */
    private static List<List<Read>> plainAbilities(List<TriggeredAbility> abilities,
                                                                                Trigger watched) {
        List<List<Read>> byAbility = Read.readByAbility(abilities, watched);
        for (List<Read> ability : byAbility) {
            Read read = ability.getFirst();
            Read.requireNoLimitOrMultiplicity(read, watched);
            if (read.filter() != null) {
                throw new IllegalStateException("The collector of " + watched + " judges no filter: "
                        + read.ability());
            }
        }
        return byAbility;
    }

    /**
     * The abilities that {@code watch} reads of {@code card} in a graveyard, its own and its perpetual ones, one list
     * for each ability (CR 603.3), as their collector reads them. No graveyard combat ability is written with a limit
     * or "each blocker", and this throws on one.
     */
    private List<TriggeredAbility> graveyardCombatAbilities(GameData gameData, Card card, Trigger watch) {
        List<TriggeredAbility> abilities = WatchedAbilities.withWatchedTrigger(
                triggerIndex.graveyardAbilities(gameData, card), watch,
                Zone.GRAVEYARD);
        for (TriggeredAbility ability : abilities) {
            if (ability.options().limit() != TriggerLimit.NONE
                    || ability.options().multiplicity() == TriggerMultiplicity.EACH_BLOCKER) {
                throw new IllegalStateException("The graveyard combat damage collector reads no " + ability.options()
                        + ": " + ability);
            }
        }
        return abilities;
    }

    /**
     * The abilities that {@code watch} reads of {@code permanent}: its printed ones, then those granted until end of
     * turn, combat or next upkeep, until it leaves the battlefield, and by other permanents' static abilities, in that
     * order.
     */
    private List<TriggeredAbility> allWatchedAbilities(GameData gameData, Permanent permanent, Trigger watch) {
        Trigger watched = watch;
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.printed(permanent.getCard(), watch, Zone.BATTLEFIELD));
        abilities.addAll(WatchedAbilities.temporary(permanent, watched));
        abilities.addAll(WatchedAbilities.persistent(permanent, watched));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched));
        return abilities;
    }

    /**
     * The abilities that {@code watch} reads granted to {@code permanent}, one list of effects for each ability
     * (CR 603.3): those until end of turn, combat or next upkeep, those until it leaves the battlefield, then those
     * other permanents' static abilities grant it, in that order, for a collector that reads no filter, limit or
     * multiplicity.
     */
    private List<TriggeredAbility> grantedCombatAbilities(GameData gameData, Permanent permanent, Trigger watch) {
        Trigger watched = watch;
        List<TriggeredAbility> granted = new ArrayList<>(WatchedAbilities.temporary(permanent, watched));
        granted.addAll(WatchedAbilities.persistent(permanent, watched));
        granted.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched));
        plainAbilities(granted, watched);
        return granted;
    }

    /**
     * The combat damage each player was dealt in this combat damage step, every source's summed (CR 510.2: it is
     * dealt simultaneously, one event): the damage assigned to a player and the damage redirected to one.
     */
    private Map<UUID, Integer> combatDamageByPlayer(CombatDamageState state, UUID defenderId) {
        Map<UUID, Integer> damageByPlayer = new LinkedHashMap<>();
        state.combatDamageDealtToPlayer.forEach((source, damage) -> {
            if (damage > 0) {
                damageByPlayer.merge(state.combatDamagePlayerRecipients.getOrDefault(source, defenderId), damage,
                        Integer::sum);
            }
        });
        state.redirectedCombatDamageToPlayers.forEach((playerId, damageBySource) -> damageBySource.values()
                .forEach(damage -> {
                    if (damage > 0) damageByPlayer.merge(playerId, damage, Integer::sum);
                }));
        return damageByPlayer;
    }

    private void processSourceDealsDamageReflectionTriggers(GameData gameData, CombatDamageState state,
                                                             UUID defenderId,
                                                             Map<UUID, Permanent> damagedCreatureSnapshots) {
        for (var entry : state.combatDamageDealt.entrySet()) {
            Permanent source = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            // Prefer the controller captured while the source was alive (it may have died dealing
            // damage to a blocker); fall back to a live lookup for still-present sources.
            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            int damageToDefender = state.combatDamageDealtToPlayer.getOrDefault(source, 0);
            Map<UUID, Integer> damageToPermanents = new HashMap<>();
            state.combatDamageAmountsToCreatures.getOrDefault(source, Map.of())
                    .forEach((permanentId, damage) -> damageToPermanents.merge(permanentId, damage, Integer::sum));
            state.combatDamageAmountsToPlaneswalkers.getOrDefault(source, Map.of())
                    .forEach((permanentId, damage) -> damageToPermanents.merge(permanentId, damage, Integer::sum));
            state.combatDamageAmountsToBattles.getOrDefault(source, Map.of())
                    .forEach((permanentId, damage) -> damageToPermanents.merge(permanentId, damage, Integer::sum));
            damageTriggers.queueSourceDealsDamageReflections(
                    gameData, source.getCard(), controllerId, source.getId(), damageDealt,
                    damageToDefender > 0 ? Map.of(
                            state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                            damageToDefender) : Map.of(),
                    state.selfDealsDamageAbilities.get(source), null, damageToPermanents, true,
                    damagedCreatureSnapshots);
        }
    }

    private void snapshotSelfDealsDamageAbilities(GameData gameData, CombatDamageState state) {
        Trigger selfDealsDamage = Triggers.dealsDamage();
        for (Permanent source : state.combatDamageDealt.keySet()) {
            List<TriggeredAbility> abilities = new ArrayList<>();
            if (!source.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, source)) {
                abilities.addAll(Read.printed(source.getCard(), Triggers.dealsDamage()));
            }
            abilities.addAll(WatchedAbilities.temporary(source, selfDealsDamage));
            abilities.addAll(WatchedAbilities.persistent(source, selfDealsDamage));
            abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, source, selfDealsDamage));
            state.selfDealsDamageAbilities.put(source, abilities);
        }
    }

    private void processSelfDealsCombatDamageTriggers(GameData gameData, CombatDamageState state,
                                                       UUID defenderId,
                                                       Map<UUID, List<Permanent>> allyCombatDamageWatchers) {
        for (var entry : state.combatDamageDealt.entrySet()) {
            Permanent source = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            // The dealer's controller is only captured on the creature-damage path, so fall back to a
            // live lookup for a creature whose damage went to a player or planeswalker only.
            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            damageTriggers.queueSourceDealsCombatDamageTriggers(
                    gameData, source.getCard(), controllerId, source.getId(), damageDealt,
                    state.combatDamageDealtToPlayer.getOrDefault(source, 0),
                    state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                    state.selfDealsCombatDamageAbilities.get(source),
                    allyCombatDamageWatchers.getOrDefault(controllerId, List.of()));
        }
    }

    private void processCombatDamageToPlayerTriggers(GameData gameData, Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                     UUID attackerId, UUID defenderId, Map<Permanent, UUID> attachedDamageSources,
                                                     List<com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers.AllyDamageTriggerSnapshot> allyDamageWatchers,
                                                     Map<UUID, List<Permanent>> departedWatchersByController,
                                                     Set<String> firedOncePerStepAbilities) {
        // Sources of batched ally triggers ("whenever one or more ... deal combat damage to a
        // player") that already fired in this damage step against this player, so they fire once
        // for the whole batch instead of once per dealer.
        Set<String> firedBatchedAllyAbilities = new HashSet<>();
        // The planes' "one or more creatures you control deal combat damage to a player" abilities that already
        // triggered in this damage step against this player, each by its place among the plane's abilities.
        Set<String> firedBatchedPlanarAbilities = new HashSet<>();
        damageTriggers.checkControllerDealtCombatDamageTriggers(gameData, defenderId,
                combatDamageDealtToPlayer.values().stream().mapToInt(Integer::intValue).sum());
        damageTriggers.checkGoadedCreatureCombatDamageToOpponentTriggers(
                gameData,
                defenderId,
                combatDamageDealtToPlayer.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList());
        damageTriggers.checkAllyCreaturesDealDamageToOpponentTriggers(
                gameData, attackerId, defenderId,
                combatDamageDealtToPlayer.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList(), allyDamageWatchers);
        damageTriggers.checkAllyCreaturesDealDamageToPlayerTriggers(
                gameData, attackerId, defenderId,
                combatDamageDealtToPlayer.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList());
        // Graveyard cards whose "whenever one or more creatures you control deal combat damage"
        // trigger has already fired for this damage event — such a trigger fires only once no
        // matter how many creatures connected.
        for (var entry : combatDamageDealtToPlayer.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            gameData.recordCombatDamageByCreatureNameToPlayer(creature.getCard().getName(), defenderId);
            gameData.combatDamageToPlayersThisTurn
                    .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(defenderId);
            gameData.combatDamageToPlayersThisCombat
                    .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(defenderId);
            gameData.playersDealtCombatDamageSinceTheirLastTurn.add(defenderId);
            gameData.recordCreatureDamageSourceToPlayer(creature.getId(), defenderId);
            if (gameData.isCommander(creature.getOriginalCard().getId())) {
                gameData.combatDamageSourcesThatWereCommandersThisTurn.add(creature.getId());
            }
            if (gameQueryService.hasEffectiveSupertype(gameData, creature, CardSupertype.LEGENDARY)) {
                gameData.combatDamageSourcesWithLegendaryThisTurn.add(creature.getId());
                gameData.legendaryCreatureCombatDamageToPlayersThisTurn
                        .computeIfAbsent(creature.getId(), ignored -> ConcurrentHashMap.newKeySet())
                        .add(defenderId);
            }

            // Record creature subtypes at combat damage time for subtype-conditional triggers
            // (e.g. Admiral Beckett Brass checks if 3+ Pirates dealt damage to a player)
            gameData.combatDamageSourceNamesThisTurn.putIfAbsent(creature.getId(), creature.getCard().getName());
            {
                Set<CardSubtype> effectiveSubtypes = gameData.combatDamageSourceSubtypesThisTurn
                        .computeIfAbsent(creature.getId(), ignored -> ConcurrentHashMap.newKeySet());
                effectiveSubtypes.addAll(creature.getCard().getSubtypes());
                effectiveSubtypes.addAll(creature.getGrantedSubtypes());
                effectiveSubtypes.addAll(creature.getTransientSubtypes());
                if (gameQueryService.hasKeyword(gameData, creature, Keyword.CHANGELING)) {
                    gameData.combatDamageSourcesWithChangelingThisTurn.add(creature.getId());
                }
            }

            // Record the controller's subtypes at combat damage time so prowl (an alternative
            // cost gated on "you dealt combat damage to a player this turn with a [subtype]") can be
            // evaluated regardless of whether the source is still on the battlefield.
            gameData.combatDamageToPlayerControllerSubtypesThisTurn
                    .computeIfAbsent(attackerId, k -> ConcurrentHashMap.newKeySet())
                    .addAll(gameData.combatDamageSourceSubtypesThisTurn.get(creature.getId()));
            gameData.combatDamageDealersByControllerThisTurn
                    .computeIfAbsent(attackerId, k -> ConcurrentHashMap.newKeySet())
                    .add(creature.getId());
            if (gameData.combatDamageSourcesWithChangelingThisTurn.contains(creature.getId())) {
                gameData.controllersDealtCombatDamageWithChangelingThisTurn.add(attackerId);
            }

            // CR 603.4 — only an ability's intervening "if" (and its event condition, CR 603.2) gates whether it
            // triggers; an "if" of an effect is judged as it resolves. The creature that dealt the damage is the
            // event's permanent ("if it's not a token", Semblance Scanner).
            ConditionContext damageContext = ConditionContext.forPermanent(creature, attackerId).withTargetId(defenderId)
                    .withTriggeringPermanentId(creature.getId()).withEventValue(damageDealt);
            // A creature that has lost all abilities also loses the abilities it was permanently granted
            // earlier (e.g. an encoded cipher spell's trigger).
            boolean lostAllAbilities = creature.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.computeStaticBonus(gameData, creature).losesAllAbilities();
            // An animated noncreature Equipment that is not attached is not an equipped creature, so its
            // printed "equipped creature deals combat damage" triggers do not apply. Equipment creatures
            // (reconfigure) keep theirs: they read "this creature or equipped creature".
            boolean unattachedEquipment = creature.getAttachedTo() == null
                    && GameQueryService.permanentHasSubtype(creature, CardSubtype.EQUIPMENT)
                    && !creature.getCard().hasType(CardType.CREATURE);
            boolean hasPrintedAbilities = !creature.isFaceDown() && !lostAllAbilities && !unattachedEquipment;
            // Each ability of the creature, whoever gave it to it, is one object on the stack (CR 603.3); two
            // abilities are two objects (CR 603.3b).
            Trigger combatDamageToPlayer = Triggers.dealsCombatDamageToPlayer();
            Trigger damageToPlayer = Triggers.dealsDamageToPlayer();
            Trigger damageToOpponent = Triggers.dealsDamageToOpponent();
            List<TriggeredAbility> triggered = new ArrayList<>();
            if (hasPrintedAbilities) {
                triggered.addAll(plainCombatAbilities(WatchedAbilities.printed(creature.getCard(), Triggers.dealsCombatDamageToPlayer(), Zone.BATTLEFIELD), combatDamageToPlayer));
                triggered.addAll(plainDamageAbilities(Read.printed(creature.getCard(),
                        Triggers.dealsDamageToPlayer()), damageToPlayer));
            }
            if (!attackerId.equals(defenderId)) {
                if (hasPrintedAbilities) {
                    triggered.addAll(plainDamageAbilities(Read.printed(creature.getCard(),
                            Triggers.dealsDamageToOpponent()), damageToOpponent));
                }
                triggered.addAll(plainDamageAbilities(WatchedAbilities.temporary(creature, damageToOpponent),
                        damageToOpponent));
                if (!lostAllAbilities) {
                    triggered.addAll(plainDamageAbilities(WatchedAbilities.persistent(creature, damageToOpponent),
                            damageToOpponent));
                }
                triggered.addAll(plainDamageAbilities(grantedTriggeredAbilitySupport.grantedWatchedAbilities(
                        gameData, creature, attackerId, damageToOpponent), damageToOpponent));
            }
            // Combat-damage-to-player triggers granted until end of turn by one-shot effects
            // (e.g. Open into Wonder grants "deals combat damage to a player, draw a card"), and "deals damage to a
            // player" ones granted the same way: an ability of the creature whatever its duration.
            triggered.addAll(plainCombatAbilities(WatchedAbilities.temporary(creature, combatDamageToPlayer),
                    combatDamageToPlayer));
            triggered.addAll(plainDamageAbilities(WatchedAbilities.temporary(creature, damageToPlayer),
                    damageToPlayer));
            if (!lostAllAbilities) {
                triggered.addAll(plainCombatAbilities(WatchedAbilities.persistent(creature, combatDamageToPlayer),
                        combatDamageToPlayer));
                triggered.addAll(plainDamageAbilities(WatchedAbilities.persistent(creature, damageToPlayer),
                        damageToPlayer));
            }
            // Combat-damage-to-player triggers granted continuously by a static ability
            // (e.g. Tandem Lookout's soulbond grant), recomputed by the layer system.
            triggered.addAll(plainCombatAbilities(grantedTriggeredAbilitySupport.grantedWatchedAbilities(
                    gameData, creature, attackerId, combatDamageToPlayer), combatDamageToPlayer));
            triggered.addAll(plainDamageAbilities(grantedTriggeredAbilitySupport.grantedWatchedAbilities(
                    gameData, creature, attackerId, damageToPlayer), damageToPlayer));
            TriggerContext.SourceDealsCombatDamage combatDamageContext = new TriggerContext.SourceDealsCombatDamage(
                    creature.getCard(), attackerId, creature.getId(), damageDealt, damageDealt);
            for (TriggeredAbility ability : triggered) {
                // CR 603.2: the event condition is judged as the ability triggers; the creature that dealt the
                // damage is the event's permanent ("if it's not a token", Semblance Scanner).
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), damageContext)) {
                    continue;
                }
                // "That player" and "that many" are the player dealt damage and the damage dealt, which the entry
                // records whatever the effects are: the player as its triggering player, attacked target and
                // non-targeting target, the damage as its event value, the creature as the damage's source.
                boolean put = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        creature.getCard(), creature, attackerId, creature.getCard().getName() + "'s triggered ability",
                        damageContext, record -> {
                            record.setTargetId(defenderId);
                            record.setNonTargeting(true);
                            record.setSourcePermanentSnapshot(new Permanent(creature));
                            record.setAlternateCost(creature.isAlternateCost());
                            record.setEventValue(damageDealt);
                            record.setTriggeringPlayerId(defenderId);
                            record.setAttackedTargetId(defenderId);
                            record.setEventDamageSource(creature, attackerId);
                        }).withEvent(combatDamageContext));
                if (put) {
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                            "'s combat damage trigger goes on the stack."));
                }
            }

            checkAttachedCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId, damageDealt,
                    attachedDamageSources);
            checkPlayerAttachedCurseCombatDamageTriggers(gameData, creature, attackerId, defenderId, damageDealt);
            checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId, damageDealt,
                    combatDamageDealtToPlayer, firedBatchedAllyAbilities, false, false,
                    departedWatchersByController.getOrDefault(attackerId, List.of()));
            for (UUID otherPlayerId : gameData.orderedPlayerIds) {
                if (otherPlayerId.equals(attackerId)) continue;
                checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, otherPlayerId, defenderId,
                        damageDealt, combatDamageDealtToPlayer, firedBatchedAllyAbilities, false, true,
                        departedWatchersByController.getOrDefault(otherPlayerId, List.of()));
            }
            checkSameNameCreatureCombatDamageToPlayerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt);
            damageTriggers.checkPlanarAllyCreatureCombatDamageToPlayerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt, firedBatchedPlanarAbilities);
            damageTriggers.checkAnyCreatureCombatDamageToOpponentTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt, firedOncePerStepAbilities);
            damageTriggers.checkAnyCreatureCombatDamageToOwnerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt);
        }
    }

    /**
     * Checks global triggers that watch for a creature with the same name as the watcher dealing
     * combat damage to a player. The watcher need not be controlled by the creature's controller;
     * this models text such as Pirated Copy's "this creature or another creature with the same name".
     */
    private void checkSameNameCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                    UUID attackerId,
                                                                    UUID defenderId,
                                                                    int damageDealt) {
        Trigger sameName = Triggers.creatureWithSameNameDealsCombatDamageToPlayer();
        TriggerContext.SourceDealsCombatDamage combatDamageContext = new TriggerContext.SourceDealsCombatDamage(
                creature.getCard(), attackerId, creature.getId(), damageDealt, damageDealt);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            FilterContext triggerContext = FilterContext.of(gameData)
                    .withSourceCardId(watcher.getCard().getId())
                    .withSourceControllerId(watcherControllerId)
                    .withSourcePermanentId(watcher.getId());
            // The watched trigger's own filter, "a creature with the same name as this", which its watch key keeps.
            if (!predicateEvaluationService.matchesPermanentPredicate(creature, SAME_NAME_AS_SOURCE, triggerContext)) {
                return;
            }
            ConditionContext context = ConditionContext.forPermanent(watcher, watcherControllerId)
                    .withTargetId(defenderId).withTriggeringPermanentId(creature.getId()).withEventValue(damageDealt);
            // Each ability is one object of all its effects (CR 603.3); "that player" is the player dealt damage, the
            // entry's non-targeting target, and "that much" the damage, its event value.
            for (TriggeredAbility ability : allWatchedAbilities(gameData, watcher,
                    Triggers.creatureWithSameNameDealsCombatDamageToPlayer())) {
                if (ability.options().limit() != TriggerLimit.NONE
                        || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                    throw new IllegalStateException("The collector of " + sameName + " reads no "
                            + ability.options() + ": " + ability);
                }
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        watcher.getCard(), watcher, watcherControllerId,
                        watcher.getCard().getName() + "'s triggered ability", context, entry -> {
                            entry.setTargetId(defenderId);
                            entry.setEventValue(damageDealt);
                            entry.setNonTargeting(true);
                        }).withEvent(combatDamageContext));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.cardThen(watcher.getCard(),
                            "'s same-name combat damage trigger goes on the stack."));
                }
            }
        });
    }

    /**
     * "Whenever enchanted / equipped creature deals combat damage to a player" of the Auras and Equipment attached to
     * {@code creature} as the damage was dealt: each ability, printed or granted to the attached permanent, is judged
     * once and is one object on the stack (CR 603.3); two abilities of one Equipment are two objects (CR 603.3b).
     */
    private void checkAttachedCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                           UUID defenderId, int damageDealt,
                                                           Map<Permanent, UUID> attachedDamageSources) {
        attachedDamageSources.forEach((perm, ownerId) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(creature.getId())
                    || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                return;
            }
            // Each ability's event condition ("deals combat damage to an opponent", Sleeper's Robe; CR 603.2) and
            // intervening "if" (CR 603.4; "if it's not a token", Semblance Scanner), read from the attached permanent
            // and its controller against the player dealt damage and the creature that dealt it as the ability
            // triggers; the intervening "if" stays wrapped for the check as it resolves.
            ConditionContext attachedContext = ConditionContext.forPermanent(perm, ownerId).withTargetId(defenderId)
                    .withTriggeringPermanentId(creature.getId()).withEventValue(damageDealt);
            Trigger combatDamageToPlayer = Triggers.dealsCombatDamageToPlayer();
            Trigger damageToPlayer = Triggers.dealsDamageToPlayer();
            Trigger damageToOpponent = Triggers.dealsDamageToOpponent();
            List<TriggeredAbility> abilities = new ArrayList<>();
            // An ability that says "this creature or equipped creature deals combat damage to a player" (Komainu
            // Battle Armor) triggers for the equipped creature through that condition, which its own collector reads;
            // read here too it would trigger twice for one event (CR 603.2c).
            Trigger equippedDealsCombatDamage =
                    Triggers.equippedCreatureDealsCombatDamageToPlayer();
            abilities.addAll(plainCombatAbilities(WatchedAbilities.printed(perm.getCard(), Triggers.dealsCombatDamageToPlayer(), Zone.BATTLEFIELD).stream()
                    .filter(ability -> !ability.hasWatchedTrigger(equippedDealsCombatDamage)).toList(),
                    combatDamageToPlayer));
            abilities.addAll(plainDamageAbilities(Read.printed(perm.getCard(),
                    Triggers.dealsDamageToPlayer()), damageToPlayer));
            abilities.addAll(plainCombatAbilities(WatchedAbilities.temporary(perm, combatDamageToPlayer),
                    combatDamageToPlayer));
            abilities.addAll(plainDamageAbilities(WatchedAbilities.temporary(perm, damageToPlayer), damageToPlayer));
            abilities.addAll(plainCombatAbilities(WatchedAbilities.persistent(perm, combatDamageToPlayer),
                    combatDamageToPlayer));
            abilities.addAll(plainDamageAbilities(WatchedAbilities.persistent(perm, damageToPlayer), damageToPlayer));
            if (!ownerId.equals(defenderId)) {
                abilities.addAll(plainDamageAbilities(Read.printed(perm.getCard(),
                        Triggers.dealsDamageToOpponent()), damageToOpponent));
                abilities.addAll(plainDamageAbilities(WatchedAbilities.temporary(perm, damageToOpponent),
                        damageToOpponent));
                abilities.addAll(plainDamageAbilities(WatchedAbilities.persistent(perm, damageToOpponent),
                        damageToOpponent));
            }
            TriggerContext.SourceDealsCombatDamage combatDamageContext = new TriggerContext.SourceDealsCombatDamage(
                    creature.getCard(), attackerId, creature.getId(), damageDealt, damageDealt);
            for (TriggeredAbility ability : abilities) {
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), attachedContext)) {
                    continue;
                }
                // The ability is the Aura's or Equipment's own (it deals damage itself, Sword of War and Peace);
                // "that player" is the player dealt damage, and "it", "that creature" and "its controller" the
                // creature that dealt it, its triggering permanent, whatever the effects are.
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        perm.getCard(), perm, ownerId, perm.getCard().getName() + "'s triggered ability",
                        attachedContext, entry -> {
                            entry.setTargetId(defenderId);
                            entry.setSourcePermanentSnapshot(new Permanent(perm));
                            entry.setTriggeringPlayerId(defenderId);
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(attackerId);
                            entry.setTriggeringPermanent(creature);
                            entry.setAttachedPermanentSnapshot(new Permanent(creature));
                            entry.setEventDamageSource(creature, attackerId);
                            entry.setEventValue(damageDealt);
                            entry.setNonTargeting(true);
                        }).withEvent(combatDamageContext));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                            "'s combat damage trigger goes on the stack."));
                }
            }
        });
    }

    /**
     * Checks for curse enchantments attached to the defending player that trigger on combat damage.
     * E.g. Curse of Stalked Prey: "Whenever a creature deals combat damage to enchanted player,
     * put a +1/+1 counter on that creature."
     */
    private void checkPlayerAttachedCurseCombatDamageTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                              UUID defenderId, int damageDealt) {
        Trigger combatDamageToPlayer = Triggers.dealsCombatDamageToPlayer();
        TriggerContext.SourceDealsCombatDamage combatDamageContext = new TriggerContext.SourceDealsCombatDamage(
                creature.getCard(), attackerId, creature.getId(), damageDealt, damageDealt);
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || !defenderId.equals(perm.getAttachedTo())) {
                return;
            }
            ConditionContext context = ConditionContext.forPermanent(perm, ownerId).withTargetId(defenderId)
                    .withTriggeringPermanentId(creature.getId()).withEventValue(damageDealt);
            // Each ability is one object of all its effects (CR 603.3). "That creature" is the creature that dealt
            // the damage (the entry's triggering permanent) and "that player" the enchanted player it was dealt to.
            for (TriggeredAbility ability : plainCombatAbilities(
                    WatchedAbilities.printed(perm.getCard(), Triggers.dealsCombatDamageToPlayer(), Zone.BATTLEFIELD),
                    combatDamageToPlayer)) {
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        perm.getCard(), perm, ownerId, perm.getCard().getName() + "'s triggered ability", context,
                        entry -> {
                            entry.setTargetId(defenderId);
                            entry.setTriggeringPlayerId(defenderId);
                            entry.setTriggeringPermanent(creature);
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(attackerId);
                            entry.setEventValue(damageDealt);
                            entry.setNonTargeting(true);
                        }).withEvent(combatDamageContext));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                            "'s combat damage trigger goes on the stack."));
                }
            }
        });
    }

    /**
     * Checks for permanents the attacker controls that trigger when an ally creature deals
     * combat damage to a player. E.g. Rakish Heir: "Whenever a Vampire you control deals
     * combat damage to a player, put a +1/+1 counter on it."
     *
     * <p>When {@code ownerRelativePass} is set, {@code attackerId} is a player other than the
     * dealer's controller and only abilities keyed to a commander that player owns ("your
     * commander", e.g. Jocasta, Automaton Avenger) are considered, since those still trigger while
     * another player controls the commander. The dealer predicate decides whether the dealer
     * qualifies for that player.
     */
    /**
     * "Whenever this creature deals combat damage to a player for the first time each turn" counts the
     * damage event itself, so damage dealt while the creature has lost its abilities still uses up the
     * first time even though no trigger fires (Raphael, Tag Team Tough).
     */
    private void markFirstTimeEachTurnSelfCombatDamageSpent(GameData gameData, Permanent perm) {
        for (List<Read> ability : Read.readByAbility(
                WatchedAbilities.printed(perm.getCard(), Triggers.creatureYouControlDealsCombatDamageToPlayer(), Zone.BATTLEFIELD),
                Triggers.creatureYouControlDealsCombatDamageToPlayer())) {
            Read read = ability.getFirst();
            if (read.limit() == TriggerLimit.ONCE_EACH_TURN
                    && read.filter() instanceof PermanentIsSourceCardPredicate) {
                read.markLimit(gameData, perm.getId());
            }
        }
    }

    private void checkAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                               UUID defenderId, int damageDealt,
                                                               Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                               Set<String> firedBatchedAllyAbilities,
                                                               boolean battleDamage, boolean ownerRelativePass) {
        checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId, damageDealt,
                combatDamageDealtToPlayer, firedBatchedAllyAbilities, battleDamage, ownerRelativePass,
                List.of());
    }

    /**
     * {@code departedWatchers} are permanents that were on the battlefield when the combat damage was
     * dealt but were put into a graveyard by state-based actions before these triggers were collected;
     * they still trigger off the damage dealt at the same time.
     */
    private void checkAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                               UUID defenderId, int damageDealt,
                                                               Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                               Set<String> firedBatchedAllyAbilities,
                                                               boolean battleDamage, boolean ownerRelativePass,
                                                               List<Permanent> departedWatchers) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        if (!battleDamage && !ownerRelativePass) {
            damageTriggers.checkEmblemAllyCreatureCombatDamageToPlayerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt);
            damageTriggers.collectTemporaryGlobalTriggers(
                    gameData, Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                    creature.getId(), damageDealt);
        }
        if (attackerBattlefield == null) return;

        List<Permanent> watchers = new ArrayList<>(attackerBattlefield);
        watchers.addAll(departedWatchers);
        for (Permanent perm : watchers) {
            if (perm.isFaceDown() || gameQueryService.computeStaticBonus(gameData, perm).losesAllAbilities()
                    || perm.isLosesAllAbilitiesUntilEndOfTurn()) {
                if (!perm.isFaceDown() && !battleDamage && !ownerRelativePass && perm.getId().equals(creature.getId())) {
                    markFirstTimeEachTurnSelfCombatDamageSpent(gameData, perm);
                }
                continue;
            }
            // Each ability, of either watch, is one object of all its effects (CR 603.3); its limit, filter,
            // "one or more" and conditions are judged and marked once, for the ability.
            if (!battleDamage) {
                collectAllyCombatDamageAbilities(gameData, perm, creature, attackerId, defenderId, damageDealt,
                        combatDamageDealtToPlayer, firedBatchedAllyAbilities, ownerRelativePass,
                        Triggers.creatureYouControlDealsCombatDamageToPlayer());
            }
            collectAllyCombatDamageAbilities(gameData, perm, creature, attackerId, defenderId, damageDealt,
                    combatDamageDealtToPlayer, firedBatchedAllyAbilities, ownerRelativePass,
                    Triggers.creatureYouControlDealsCombatDamageToPlayerOrBattle());
        }

        // Graveyard-based variant (GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER): the trigger
        // fires from the attacker's graveyard. E.g. Auntie's Snitch — "Whenever a Goblin or Rogue you
        // control deals combat damage to a player, if this card is in your graveyard, you may return
        // this card to your hand." The stack entry's source is the graveyard card itself.
        if (battleDamage) return;
        if (!ownerRelativePass) {
            checkGraveyardAllyCreatureCombatDamageTriggers(gameData, creature, attackerId,
                    Triggers.creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker(),
                    firedBatchedAllyAbilities, false);
        }
        checkGraveyardAllyCreatureCombatDamageTriggers(gameData, creature, attackerId,
                Triggers.creatureYouControlDealsCombatDamageToPlayer(), firedBatchedAllyAbilities,
                ownerRelativePass);
    }

    /**
     * Puts {@code perm}'s abilities of {@code watch}, "whenever a creature you control deals combat damage to a player
     * [or battle]", that {@code creature}'s damage triggers on the stack: its printed abilities and the ones it was
     * granted, each judged once from the ability (its limit, the trigger's filter on the creature, "one or more", its
     * event condition and intervening "if"; CR 603.2, 603.2h, 603.4) and each one object of all its effects
     * (CR 603.3). "One or more creatures deal combat damage" is one event for the combat damage step, marked for the
     * ability by its place among the permanent's abilities of the watch, so two such abilities of one permanent
     * trigger once each (CR 603.3b). A limit is marked whichever way the ability reaches the stack. A mode chosen as
     * the ability triggers is the ability's one effect (CR 603.3c); a permanent, declared player or graveyard target
     * is one choice with every effect (CR 603.3d).
     */
    private void collectAllyCombatDamageAbilities(GameData gameData, Permanent perm, Permanent creature,
                                                  UUID attackerId, UUID defenderId, int damageDealt,
                                                  Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                  Set<String> firedBatchedAllyAbilities, boolean ownerRelativePass,
                                                  Trigger watch) {
        List<TriggeredAbility> abilities = allWatchedAbilities(gameData, perm, watch);
        FilterContext triggerContext = FilterContext.of(gameData)
                .withSourceControllerId(attackerId)
                .withSourcePermanentId(perm.getId());
        for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
            TriggeredAbility ability = abilities.get(abilityIndex);
            TriggerOptions options = ability.options();
            // "Triggers only once each turn" (Ms. Marvel, Elastic Ally) is marked as the ability goes on the
            // stack, "do this only once each turn" (Calix, Guided by Fate) as the action is taken (CR 603.2h); the
            // stack door judges and marks both.
            if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN
                    && options.limit() != TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
                throw new IllegalStateException("The \"a creature you control deals combat damage to a player\" "
                        + "collector reads no " + options.limit() + ": " + ability);
            }
            if (options.multiplicity() == TriggerMultiplicity.EACH_BLOCKER) {
                throw new IllegalStateException("A combat damage ability has no \"each blocker\": " + ability);
            }
            boolean oneOrMore = options.multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
            PermanentPredicate filter = Read.filter(ability, watch);
            if (ownerRelativePass && !(filter instanceof PermanentIsCommanderPredicate commander
                    && commander.ownedBySourceController())) {
                continue;
            }
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                    creature, filter, triggerContext)) {
                continue;
            }
            // "One or more ... deal combat damage" is a single event for the whole batch,
            // so a batched ability only goes on the stack for the first matching dealer.
            if (oneOrMore && !firedBatchedAllyAbilities.add(perm.getId() + ":" + watch + ":" + abilityIndex)) {
                continue;
            }
            // The ability's event condition (a Class level, a chosen mode; CR 603.2) as it triggers.
            ConditionContext context = ConditionContext.forPermanent(perm, attackerId);
            if (!conditionEvaluationService.eventConditionMet(gameData, options, context)) {
                continue;
            }
            // "One or more creatures deal combat damage": the creatures of the batch that match the trigger,
            // which "them" and "each of them" read from the stack entry as the ability resolves.
            List<Map.Entry<Permanent, Integer>> dealers = oneOrMore
                    ? combatDamageDealtToPlayer.entrySet().stream()
                    .filter(dealer -> dealer.getValue() > 0
                            && (filter == null || predicateEvaluationService.matchesPermanentPredicate(
                            dealer.getKey(), filter, triggerContext)))
                    .toList()
                    : List.of();
            List<UUID> dealerIds = dealers.stream().map(dealer -> dealer.getKey().getId()).toList();
            int triggerDamage = oneOrMore ? dealers.stream().mapToInt(Map.Entry::getValue).sum() : damageDealt;
            // "That player" is the player dealt damage, its triggering player and non-targeting target (Oona's
            // Blackguard: "...that player discards a card"); "that many" the damage, its event value ("put that many
            // +1/+1 counters on it", Necropolis Regent); "it" / "that creature" the creature that dealt the damage,
            // which "it deals that much damage" has deal the damage, whatever the effects are.
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                    perm.getCard(), perm, attackerId, perm.getCard().getName() + "'s triggered ability", context,
                    entry -> {
                        entry.setTargetId(defenderId);
                        entry.setTriggeringPlayerId(defenderId);
                        entry.setEventValue(triggerDamage);
                        entry.setTriggeringPermanentIds(dealerIds);
                        if (!oneOrMore) {
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(attackerId);
                            entry.setTriggeringPermanent(creature);
                            entry.setEventDamageSource(creature, attackerId);
                        }
                        entry.setNonTargeting(true);
                        entry.setSourcePermanentSnapshot(new Permanent(perm));
                    }).withEvent(new TriggerContext.SourceDealsCombatDamage(
                    creature.getCard(), attackerId, creature.getId(), damageDealt, damageDealt)));
            if (triggered) {
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                        "'s triggered ability goes on the stack."));
            }
        }
    }

    /**
     * Fires {@code GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER} triggers from the
     * graveyards of every player who was dealt combat damage this step, either directly or on a
     * planeswalker they control. E.g. Vengeful Pharaoh — "Whenever combat damage is dealt to you or
     * a planeswalker you control, if this card is in your graveyard, destroy target attacking
     * creature, then put this card on top of your library."
     *
     * <p>The trigger targets, so it is routed through the {@code AttackTriggerTarget} pending-choice
     * pipeline rather than pushed straight onto the stack: the target is chosen as the ability is
     * put on the stack (CR 603.3d), and the ability is skipped entirely when no legal target exists.
     * The stack entry's source is the graveyard card itself (no source permanent).
     */
    private void checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers(GameData gameData,
                                                                       CombatDamageState state,
                                                                       UUID defenderId) {
        Set<UUID> damagedPlayerIds = new LinkedHashSet<>();
        if (state.damageToDefendingPlayer > 0 || state.poisonDamageToDefendingPlayer > 0) {
            state.combatDamageDealtToPlayer.forEach((source, amount) -> {
                if (amount > 0) damagedPlayerIds.add(
                        state.combatDamagePlayerRecipients.getOrDefault(source, defenderId));
            });
        }
        // One trigger per damaged recipient: each damaged player and each damaged planeswalker is a
        // separate "combat damage is dealt to" event, so simultaneous damage to a player and a
        // planeswalker they control triggers twice (Vengeful Pharaoh ruling, 2011-09-22).
        List<UUID> triggerRecipientIds = new ArrayList<>(damagedPlayerIds);
        for (var entry : state.damageToPlaneswalkers.entrySet()) {
            if (entry.getValue() <= 0) continue;
            UUID planeswalkerController = gameQueryService.findPermanentController(gameData, entry.getKey());
            if (planeswalkerController != null) {
                triggerRecipientIds.add(planeswalkerController);
            }
        }

        for (UUID playerId : triggerRecipientIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : new ArrayList<>(graveyard)) {
                Trigger dealtCombatDamage = Triggers.youOrYourPlaneswalkerDealtCombatDamage();
                ConditionContext context = ConditionContext.forCard(card, playerId);
                // Each ability is one object of all its effects (CR 603.3), its target chosen as it goes on the stack
                // (CR 603.3d); the entry records the card's graveyard incarnation (CR 400.7).
                for (TriggeredAbility ability : plainCombatAbilities(WatchedAbilities.withWatchedTrigger(
                                triggerIndex.graveyardAbilities(gameData, card),
                                dealtCombatDamage, Zone.GRAVEYARD), dealtCombatDamage)) {
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                        continue;
                    }
                    long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, card, null, playerId, card.getName() + "'s triggered ability", context,
                            entry -> entry.setSourceGraveyardEntryVersion(graveyardVersion)));
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.cardThen(card, "'s graveyard trigger triggers."));
                    }
                }
            }
        }
    }

    /**
     * Checks delayed combat damage loot triggers registered this turn (e.g. Jace, Cunning Castaway's +1).
     * Each fires once per combat damage step if one or more creatures the controller controls dealt
     * combat damage to a player.
     */
    private void processDelayedCombatDamageLootTriggers(GameData gameData,
                                                         Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                         UUID attackerId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageLoot.class)) return;

        // Check if any creature controlled by the trigger's controller dealt combat damage to a player
        for (DelayedCombatDamageLoot loot : gameData.getDelayedActions(DelayedCombatDamageLoot.class)) {
            boolean creatureDealtDamage = false;
            if (loot.controllerId().equals(attackerId)) {
                for (var dmgEntry : combatDamageDealtToPlayer.entrySet()) {
                    if (dmgEntry.getValue() > 0) {
                        creatureDealtDamage = true;
                        break;
                    }
                }
            }
            if (creatureDealtDamage) {
                StackEntry se = TriggeredAbilityStackService.madeAbility(
                        loot.sourceCard(),
                        loot.controllerId(),
                        loot.sourceCard().getName() + "'s delayed trigger",
                        List.of(new DrawCardEffect(loot.drawAmount()),
                                new DiscardEffect(loot.discardAmount(), DiscardRecipient.CONTROLLER))
                );
                se.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, se);
                gameLogService.append(gameData, GameLog.cardThen(loot.sourceCard(),
                        "'s delayed trigger fires — draw " + loot.drawAmount()
                                + ", discard " + loot.discardAmount() + "."));
            }
        }
    }

    private void processDelayedCombatDamageTokenTriggers(GameData gameData,
                                                           Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                           UUID attackerId,
                                                           UUID defenderId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageToken.class)) return;

        for (DelayedCombatDamageToken delayed : gameData.getDelayedActions(DelayedCombatDamageToken.class)) {
            if (!delayed.controllerId().equals(attackerId)
                    || !delayed.targetPlayerId().equals(defenderId)) {
                continue;
            }
            for (var damageEntry : combatDamageDealtToPlayer.entrySet()) {
                if (damageEntry.getValue() <= 0) continue;
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(delayed.tokenEffect())
                );
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                        "'s delayed trigger goes on the stack."));
            }
        }
    }

    private void processDelayedCombatDamageBecomeMonarchTriggers(GameData gameData,
                                                                  CombatDamageState state,
                                                                  UUID activeId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageBecomeMonarch.class)) return;

        for (DelayedCombatDamageBecomeMonarch delayed
                : gameData.getDelayedActions(DelayedCombatDamageBecomeMonarch.class)) {
            boolean creatureDealtDamage = state.combatDamageDealtToPlayer.entrySet().stream()
                    .anyMatch(entry -> entry.getValue() > 0
                            && delayed.controllerId().equals(
                            state.combatDamageDealerControllers.getOrDefault(entry.getKey(), activeId)));
            if (!creatureDealtDamage) continue;

            StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    List.of(new BecomeMonarchEffect()));
            trigger.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, trigger);
            gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                    "'s delayed trigger goes on the stack."));
        }
    }

    private void processDelayedCombatDamageDrawTriggers(GameData gameData, CombatDamageState state) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageDraw.class)) return;

        Map<UUID, Permanent> damageSources = new LinkedHashMap<>();
        for (Permanent creature : state.combatDamageDealtToPlayer.keySet()) {
            damageSources.put(creature.getId(), creature);
        }
        for (Permanent creature : state.combatDamageDealtToPlaneswalker.keySet()) {
            damageSources.put(creature.getId(), creature);
        }

        for (CombatDamageState.DelayedCombatDamageDrawQualification qualification
                : state.delayedCombatDamageDrawQualifications) {
            DelayedCombatDamageDraw delayed = qualification.delayedAction();
            for (UUID sourceId : qualification.sourceIds()) {
                Permanent creature = damageSources.get(sourceId);
                if (creature == null) continue;
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(new DrawCardEffect(1)));
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                        "'s delayed trigger fires — draw a card."));
            }
        }
    }

    private void processDelayedCombatDamageEffects(GameData gameData, CombatDamageState state) {
        if (state.combatDamageDealtToPlayer.isEmpty()
                || !gameData.hasDelayedAction(DelayedCombatDamageEffect.class)) {
            return;
        }

        for (DelayedCombatDamageEffect delayed
                : gameData.getDelayedActions(DelayedCombatDamageEffect.class)) {
            for (var entry : state.combatDamageDealtToPlayer.entrySet()) {
                Permanent creature = entry.getKey();
                if (entry.getValue() <= 0) continue;

                UUID controllerId = state.combatDamageDealerControllers.get(creature);
                if (controllerId == null) controllerId = gameData.findControllerOf(creature);
                if (!delayed.controllerId().equals(controllerId)) continue;

                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(delayed.triggerEffect()),
                        (UUID) null,
                        delayed.sourcePermanentId());
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(delayed.sourceCard()));
            }
        }
    }

    private void processDelayedCombatDamageLookAtHandAndDrawTriggers(GameData gameData,
                                                                       CombatDamageState state) {
        for (CombatDamageState.DelayedCombatDamageLookAtHandAndDrawQualification qualification
                : state.delayedCombatDamageLookAtHandAndDrawQualifications) {
            DelayedCombatDamageLookAtHandAndDraw delayed = qualification.delayedAction();
            StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    List.of(new LookAtHandEffect(), new DrawCardEffect(1)),
                    qualification.damagedPlayerId(),
                    (UUID) null
            );
            trigger.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, trigger);
            gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                    "'s delayed trigger fires — look at "
                            + gameData.playerIdToName.get(qualification.damagedPlayerId())
                            + "'s hand and draw a card."));
        }
    }

    private void processSelfDealsCombatDamageToPlayerOrPlaneswalkerTriggers(GameData gameData,
                                                                             CombatDamageState state) {
        for (Permanent source : state.combatDamageDealt.keySet()) {
            int damageDealtToPlayerOrPlaneswalker = state.combatDamageDealtToPlayer.getOrDefault(source, 0)
                    + state.combatDamageDealtToPlaneswalker.getOrDefault(source, 0);
            if (damageDealtToPlayerOrPlaneswalker <= 0) continue;

            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            damageTriggers.queueSourceDealsCombatDamageToPlayerOrPlaneswalkerTriggers(
                    gameData, source.getCard(), controllerId, source.getId(), damageDealtToPlayerOrPlaneswalker,
                    state.combatDamageDealtToPlayer.getOrDefault(source, 0),
                    state.selfDealsCombatDamageToPlayerOrPlaneswalkerAbilities.get(source));
        }
    }

    private void processSelfDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData,
                                                                       CombatDamageState state) {
        for (Permanent source : state.combatDamageDealt.keySet()) {
            int battleDamage = state.combatDamageAmountsToBattles
                    .getOrDefault(source, Map.of()).values().stream().mapToInt(Integer::intValue).sum();
            int totalDamage = state.combatDamageDealtToPlayer.getOrDefault(source, 0) + battleDamage;
            if (totalDamage <= 0) continue;

            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            damageTriggers.queueSourceDealsCombatDamageToPlayerOrBattleTriggers(
                    gameData, source.getCard(), controllerId, source.getId(), totalDamage,
                    state.combatDamageDealtToPlayer.getOrDefault(source, 0),
                    state.selfDealsCombatDamageToPlayerOrBattleAbilities.get(source));
        }
    }

    /**
     * Processes combat damage reflection triggers registered this turn (e.g. Harsh Justice).
     * For each attacking creature that dealt combat damage to a protected player this step, the
     * creature deals that much damage back to its controller. Each creature reflects separately, with
     * the attacking creature as the damage source and triggering permanent: "that much" is the entry's
     * event value and "its controller" the creature's controller as the ability resolves.
     */
    private void processCombatDamageReflectionTriggers(GameData gameData,
                                                       Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                       UUID attackerId, UUID defenderId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageReflection.class)) return;

        for (DelayedCombatDamageReflection reflection : gameData.getDelayedActions(DelayedCombatDamageReflection.class)) {
            // Combat damage to a player always goes to the defending player in this combat.
            if (!reflection.protectedPlayerId().equals(defenderId)) continue;
            for (var dmgEntry : combatDamageDealtToPlayer.entrySet()) {
                Permanent attacker = dmgEntry.getKey();
                int damage = dmgEntry.getValue();
                if (damage <= 0) continue;
                StackEntry se = TriggeredAbilityStackService.madeAbility(
                        attacker.getCard(),
                        reflection.protectedPlayerId(),
                        reflection.sourceCard().getName() + "'s reflected combat damage",
                        List.of(new DealDamageToPlayersEffect(new EventValue(),
                                DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)),
                        attackerId,
                        attacker.getId());
                se.setNonTargeting(true);
                se.setEventValue(damage);
                se.setTriggeringPermanentId(attacker.getId());
                se.setTriggeringPermanentControllerId(attackerId);
                triggeredAbilityStackService.putDelayed(gameData, se);
                gameLogService.append(gameData, GameLog.cardTextCard(reflection.sourceCard(),
                        " reflects " + damage + " combat damage from ", attacker.getCard(),
                        " to " + gameData.playerIdToName.get(attackerId) + "."));
            }
        }
    }

    /**
     * Puts "whenever this creature deals combat damage to a creature" abilities on the stack, once for each creature
     * dealt damage. The damaged creature is the entry's fixed "that creature" (its target, non-targeting) and its
     * triggering permanent, and the excess damage dealt to it in this combat damage step (CR 120.4a) is the entry's
     * event value, "that excess damage". Each ability's intervening "if" and event condition are judged against that
     * creature and that excess as the ability triggers (CR 603.4, CR 603.2).
     */
    private void processCombatDamageToCreatureTriggers(GameData gameData,
                                                        Map<Permanent, List<UUID>> combatDamageDealtToCreatures,
                                                        Map<Permanent, UUID> combatDamageDealerControllers,
                                                        Map<UUID, UUID> combatDamageTargetControllers,
                                                        Map<Permanent, List<TriggeredAbility>> damageToCreatureAbilities,
                                                        CombatDamageState state,
                                                        Map<UUID, Permanent> damagedCreatureSnapshots) {
        Map<UUID, Integer> damageByCreature = new HashMap<>();
        for (Map<UUID, Integer> damageAmounts : state.combatDamageAmountsToCreatures.values()) {
            damageAmounts.forEach((creatureId, amount) -> damageByCreature.merge(creatureId, amount, Integer::sum));
        }

        for (var entry : combatDamageDealtToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID controllerId = combatDamageDealerControllers.get(source);
            if (controllerId == null) continue;

            List<TriggeredAbility> abilities = damageToCreatureAbilities.getOrDefault(source, List.of());
            if (abilities.isEmpty()) continue;

            Trigger watched = Triggers.dealsCombatDamageToCreature();
            for (UUID damagedCreatureId : entry.getValue()) {
                int excessDamage = combatExcessDamage(gameData, state, damagedCreatureId,
                        damagedCreatureSnapshots.get(damagedCreatureId),
                        damageByCreature.getOrDefault(damagedCreatureId, 0));
                ConditionContext context = ConditionContext.forPermanent(source, controllerId)
                        .withTargetId(damagedCreatureId).withEventValue(excessDamage);
                for (TriggeredAbility ability : plainCombatAbilities(abilities, watched)) {
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                        continue;
                    }
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, source.getCard(), source, controllerId,
                            source.getCard().getName() + "'s triggered ability", context, record -> {
                                record.setTargetId(damagedCreatureId);
                                record.setSourcePermanentSnapshot(new Permanent(source));
                                record.setTriggeringPermanentId(damagedCreatureId);
                                record.setTriggeringPermanentControllerId(
                                        combatDamageTargetControllers.get(damagedCreatureId));
                                record.setNonTargeting(true);
                                record.setEventValue(excessDamage);
                            }));
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                    }
                }
            }
        }
    }

    private int combatExcessDamage(GameData gameData, CombatDamageState state, UUID damagedCreatureId,
                                   Permanent damagedCreatureSnapshot, int damage) {
        if (damage <= 0) {
            return 0;
        }
        int markedDamageBefore = state.markedDamageBeforeStep.getOrDefault(damagedCreatureId, 0);
        int lethalDamage = damagedCreatureSnapshot != null && damagedCreatureSnapshot.isDamagedByDeathtouch()
                ? 1
                : Math.max(0, state.lethalDamageThresholdBeforeStep.getOrDefault(damagedCreatureId,
                        damagedCreatureSnapshot == null
                                ? 0
                                : gameQueryService.getLethalDamageThreshold(gameData, damagedCreatureSnapshot))
                        - markedDamageBefore);
        return Math.max(0, damage - lethalDamage);
    }

    private void processCombatDamageToBlockingCreatureTriggers(GameData gameData, CombatDamageState state) {
        for (Permanent source : state.combatDamageDealtToCreatures.keySet()) {
            if (!state.combatDamageToBlockingCreatureSources.contains(source.getId())) {
                continue;
            }
            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) {
                continue;
            }
            // Each ability is one object of all its effects (CR 603.3), its event condition and intervening "if"
            // judged once as it triggers (CR 603.2, CR 603.4).
            ConditionContext context = ConditionContext.forPermanent(source, controllerId);
            for (TriggeredAbility ability : plainCombatAbilities(
                    WatchedAbilities.printed(source.getCard(), Triggers.dealsCombatDamageToBlockingCreature(), Zone.BATTLEFIELD),
                    Triggers.dealsCombatDamageToBlockingCreature())) {
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        source.getCard(), source, controllerId, source.getCard().getName() + "'s triggered ability",
                        context, TriggeredAbilityRequest.TriggerRecord.NONE));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                }
            }
        }
    }

    /**
     * Fires Acidic Dagger's delayed "whenever that creature deals combat damage to a non-Wall
     * creature this turn, destroy that non-Wall creature" triggers. The registration is not drained:
     * it lasts the rest of the turn and fires once per damaged non-Wall creature in every combat
     * damage step, until turn cleanup clears it.
     */
    private void processDelayedDestroyCreatureDamagedTriggers(GameData gameData,
                                                              Map<Permanent, List<UUID>> combatDamageDealtToCreatures) {
        if (combatDamageDealtToCreatures.isEmpty()
                || !gameData.hasDelayedAction(DelayedDestroyCreatureDamagedByWatchedCreature.class)) {
            return;
        }

        for (DelayedDestroyCreatureDamagedByWatchedCreature delayed
                : gameData.getDelayedActions(DelayedDestroyCreatureDamagedByWatchedCreature.class)) {
            for (var entry : combatDamageDealtToCreatures.entrySet()) {
                if (!delayed.watchedPermanentId().equals(entry.getKey().getId())) continue;

                for (UUID damagedCreatureId : entry.getValue()) {
                    Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
                    if (damaged == null || predicateEvaluationService.matchesPermanentPredicate(
                            gameData, damaged, new PermanentHasSubtypePredicate(CardSubtype.WALL))) {
                        continue;
                    }

                    StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                            delayed.sourceCard(),
                            delayed.controllerId(),
                            delayed.sourceCard().getName() + "'s delayed trigger",
                            new ArrayList<>(List.of(new DestroyTargetPermanentEffect())),
                            damagedCreatureId,
                            (UUID) null);
                    trigger.setNonTargeting(true);
                    triggeredAbilityStackService.putDelayed(gameData, trigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(delayed.sourceCard()));
                }
            }
        }
    }

    private void processDelayedPlaneswalkerCombatDamageTriggers(GameData gameData,
                                                                Map<Permanent, Integer> combatDamageDealtToPlaneswalker) {
        if (combatDamageDealtToPlaneswalker.isEmpty()
                || !gameData.hasDelayedAction(DelayedDestroyCreatureDealingCombatDamageToPlaneswalker.class)) {
            return;
        }

        for (DelayedDestroyCreatureDealingCombatDamageToPlaneswalker delayed
                : gameData.getDelayedActions(DelayedDestroyCreatureDealingCombatDamageToPlaneswalker.class)) {
            for (var entry : combatDamageDealtToPlaneswalker.entrySet()) {
                Permanent creature = entry.getKey();
                if (entry.getValue() <= 0
                        || !delayed.planeswalkerId().equals(creature.getAttackTarget())) {
                    continue;
                }

                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(new DestroyTargetPermanentEffect()),
                        creature.getId(),
                        (UUID) null
                );
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(delayed.sourceCard()));
            }
        }
    }

    /**
     * Fires delayed triggers watching creatures for combat damage. {@code combatDamageDealt} already
     * sums each source's damage to every recipient, so a watched creature that damaged both a blocker
     * and a player triggers once. Player-only watches additionally use the player-specific damage map.
     * The trigger is controlled by the player who activated the ability, not by the creature's
     * controller, so "you draw a card" always draws for them.
     */
    private void processDelayedWatchedCreatureCombatDamageTriggers(GameData gameData,
                                                                   CombatDamageState state) {
        if (state.combatDamageDealt.isEmpty()
                || !gameData.hasDelayedAction(DelayedWatchedCreaturesCombatDamage.class)) {
            return;
        }

        for (DelayedWatchedCreaturesCombatDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreaturesCombatDamage.class)) {
            for (var entry : state.combatDamageDealt.entrySet()) {
                if (entry.getValue() <= 0
                        || !watch.watchedPermanentIds().contains(entry.getKey().getId())
                        || (watch.combatDamageToPlayerOnly()
                        && state.combatDamageDealtToPlayer.getOrDefault(entry.getKey(), 0) <= 0)) {
                    continue;
                }

                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        watch.sourceCard(),
                        watch.controllerId(),
                        watch.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(watch.effects()),
                        (UUID) null,
                        entry.getKey().getId());
                trigger.setGrantingPermanentId(watch.creatingPermanentId());
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            }
        }
    }

    private void processDelayedNamedCreatureCombatDamageTriggers(GameData gameData,
                                                                  CombatDamageState state) {
        if (state.combatDamageDealtToPlayer.isEmpty()
                || !gameData.hasDelayedAction(DelayedNamedCreatureCombatDamage.class)) {
            return;
        }

        for (DelayedNamedCreatureCombatDamage watch
                : gameData.getDelayedActions(DelayedNamedCreatureCombatDamage.class)) {
            for (var entry : state.combatDamageDealtToPlayer.entrySet()) {
                Permanent creature = entry.getKey();
                if (entry.getValue() <= 0
                        || !watch.cardName().equals(gameQueryService.getEffectiveName(gameData, creature))) {
                    continue;
                }

                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        watch.sourceCard(),
                        watch.controllerId(),
                        watch.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(watch.effects()),
                        (UUID) null,
                        (UUID) null);
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            }
        }
    }

    /**
     * Collects the self-scoped ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers of each combat-damage
     * source. Runs before state-based actions so a source that dies to the same damage still triggers.
     */
    private void processSelfDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state,
                                                                     Map<UUID, Permanent> damagedCreatureSnapshots) {
        forEachCombatDamageToCreaturePair(gameData, state, damagedCreatureSnapshots,
                (source, sourceControllerId, damagedCreatureControllerId, damagedCreatureId,
                 damagedCreatureSnapshot, damage) ->
                        damageTriggers.checkSelfDealtDamageToCreatureTriggers(
                                gameData, source, sourceControllerId, damagedCreatureControllerId,
                                damagedCreatureId, damagedCreatureSnapshot, damage, true));
    }

    /**
     * Collects the ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers that other permanents watch
     * (e.g. Greatbow Doyen, Ohabi Caleria) for each source/target combat-damage pair. Runs before
     * state-based actions so a watcher that dies to the same damage still triggers.
     */
    private void processWatchersDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state,
                                                                         Map<UUID, Permanent> damagedCreatureSnapshots) {
        forEachCombatDamageToCreaturePair(gameData, state, damagedCreatureSnapshots,
                (source, sourceControllerId, damagedCreatureControllerId, damagedCreatureId,
                 damagedCreatureSnapshot, damage) ->
                        damageTriggers.checkWatchersDealtDamageToCreatureTriggers(
                                gameData, source, sourceControllerId, damagedCreatureControllerId,
                                damagedCreatureId, damagedCreatureSnapshot, damage, true));
    }

    /**
     * Fires the creature-damaged-by-source triggers that depend on the damaged creature's snapshot
     * for each source/target combat-damage pair.
     */
    private void processAllyDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state,
                                                                     Map<UUID, Permanent> damagedCreatureSnapshots) {
        forEachCombatDamageToCreaturePair(gameData, state, damagedCreatureSnapshots,
                (source, sourceControllerId, damagedCreatureControllerId, damagedCreatureId,
                 damagedCreatureSnapshot, damage) -> {
                    if (damagedCreatureSnapshot != null) {
                        damageTriggers.checkCreatureDamageToYouOrYourPermanentTriggers(
                                gameData, damagedCreatureControllerId, damagedCreatureSnapshot, source, damage);
                    }
                });
    }

    private void forEachCombatDamageToCreaturePair(GameData gameData, CombatDamageState state,
                                                    Map<UUID, Permanent> damagedCreatureSnapshots,
                                                    CombatDamagePairAction action) {
        for (var entry : state.combatDamageAmountsToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) continue;
            for (var amountEntry : entry.getValue().entrySet()) {
                UUID damagedCreatureControllerId = state.combatDamageTargetControllers.get(amountEntry.getKey());
                Permanent damagedCreatureSnapshot = gameQueryService.findPermanentById(gameData, amountEntry.getKey());
                if (damagedCreatureSnapshot == null) {
                    damagedCreatureSnapshot = damagedCreatureSnapshots.get(amountEntry.getKey());
                }
                action.accept(source, sourceControllerId, damagedCreatureControllerId, amountEntry.getKey(),
                        damagedCreatureSnapshot, amountEntry.getValue());
            }
        }
    }

    @FunctionalInterface
    private interface CombatDamagePairAction {
        void accept(Permanent source, UUID sourceControllerId, UUID damagedCreatureControllerId,
                    UUID damagedCreatureId, Permanent damagedCreatureSnapshot, int damage);
    }

    /**
     * "Whenever a creature you control deals combat damage to a player [or planeswalker]" from {@code attackerId}'s
     * graveyard ({@code watch}): each ability of each card there is judged once (the trigger's filter on the creature,
     * "one or more", marked for the ability by its place among the card's abilities of the watch, its event condition
     * and intervening "if") and is one object of all its effects (CR 603.3). The entry's source is the graveyard card
     * itself and keeps this graveyard incarnation (CR 400.7), so "if this card is in your graveyard" is false once the
     * card has left and returned to the graveyard. On the {@code ownerRelativePass}, {@code attackerId} is a player
     * other than the dealer's controller and only abilities keyed to a commander that player owns are read.
     */
    private void checkGraveyardAllyCreatureCombatDamageTriggers(GameData gameData, Permanent creature,
                                                                 UUID attackerId, Trigger watch,
                                                                 Set<String> firedBatchedAllyAbilities,
                                                                 boolean ownerRelativePass) {
        List<Card> graveyard = gameData.playerGraveyards.get(attackerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            List<TriggeredAbility> abilities = graveyardCombatAbilities(gameData, card, watch);
            ConditionContext context = ConditionContext.forCard(card, attackerId)
                    .withTriggeringPermanentId(creature.getId());
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                TriggeredAbility ability = abilities.get(abilityIndex);
                PermanentPredicate filter = Read.filter(ability, watch);
                if (ownerRelativePass && !(filter instanceof PermanentIsCommanderPredicate commander
                        && commander.ownedBySourceController())) {
                    continue;
                }
                if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature,
                        filter)) {
                    continue;
                }
                if (ability.options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE
                        && !firedBatchedAllyAbilities.add(card.getId() + ":" + watch + ":" + abilityIndex)) {
                    continue;
                }
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                // "This card" is the graveyard card as it is now: one that leaves and comes back is a new object
                // (CR 400.7), which the entry's graveyard incarnation tells apart.
                long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        card, null, attackerId, card.getName() + "'s graveyard trigger", context, entry -> {
                            entry.setTriggeringCardId(card.getId());
                            entry.setTriggeringCardGraveyardEntryVersion(graveyardVersion);
                            entry.setSourceGraveyardEntryVersion(graveyardVersion);
                            entry.setNonTargeting(true);
                        }));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.cardThen(card,
                            "'s graveyard trigger goes on the stack."));
                }
            }
        }
    }

    private void processAllyDealtDamageToPlaneswalkerTriggers(GameData gameData, CombatDamageState state) {
        Set<String> firedBatchedAllyAbilities = new HashSet<>();
        for (var entry : state.combatDamageAmountsToPlaneswalkers.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) continue;
            for (var amountEntry : entry.getValue().entrySet()) {
                if (amountEntry.getValue() > 0) {
                    checkGraveyardAllyCreatureCombatDamageTriggers(gameData, source, sourceControllerId,
                            Triggers.creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker(),
                            firedBatchedAllyAbilities, false);
                }
                damageTriggers.checkAllyDealtDamageToPlaneswalkerTriggers(
                        gameData, source, sourceControllerId, amountEntry.getKey(), amountEntry.getValue(), true);
            }
        }
    }

    /**
     * Captures the damaged creatures before state-based actions remove lethal casualties.
     */
    private Map<UUID, Permanent> snapshotCombatDamagedCreatures(GameData gameData, CombatDamageState state) {
        Map<UUID, Permanent> damagedCreatures = new LinkedHashMap<>();
        for (var entry : state.combatDamageAmountsToCreatures.entrySet()) {
            for (var amountEntry : entry.getValue().entrySet()) {
                if (amountEntry.getValue() <= 0) continue;
                Permanent damaged = gameQueryService.findPermanentById(gameData, amountEntry.getKey());
                if (damaged != null) {
                    damagedCreatures.putIfAbsent(damaged.getId(), damaged);
                }
            }
        }
        return damagedCreatures;
    }

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers for each creature that took combat damage this
     * step, using the captured creature and controller when lethal damage removed it.
     */
    private void processAnyCreatureDealtDamageTriggers(GameData gameData, CombatDamageState state,
                                                       Map<UUID, Permanent> damagedCreatureSnapshots) {
        Map<UUID, Integer> damageByCreature = new LinkedHashMap<>();
        for (var entry : state.combatDamageDealtToCreatures.entrySet()) {
            Map<UUID, Integer> amounts = state.combatDamageAmountsToCreatures.getOrDefault(entry.getKey(), Map.of());
            for (UUID targetId : entry.getValue()) {
                int damage = amounts.getOrDefault(targetId, 0);
                if (damage > 0) {
                    damageByCreature.merge(targetId, damage, Integer::sum);
                }
            }
        }
        for (var entry : damageByCreature.entrySet()) {
            UUID damagedCreatureId = entry.getKey();
            Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
            if (damaged == null) {
                damaged = damagedCreatureSnapshots.get(damagedCreatureId);
            }
            if (damaged == null) continue;

            UUID controllerId = state.combatDamageTargetControllers.get(damagedCreatureId);
            damageTriggers.checkAnyCreatureDealtDamageTriggers(
                    gameData, damaged, controllerId, entry.getValue());
            damageTriggers.checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
                    gameData, damaged, controllerId, entry.getValue());
        }
    }

    /**
     * Last-known information of a creature that was just dealt damage, kept for its "dealt damage"
     * triggers even if it dies before they are put on the stack. It records the keywords the creature
     * had (CR 608.2h) so damage its trigger deals still has lifelink, deathtouch, infect or wither.
     */
    private Permanent lastKnownDamagedSnapshot(GameData gameData, Permanent creature) {
        Permanent snapshot = new Permanent(creature);
        for (Keyword keyword : List.of(Keyword.LIFELINK, Keyword.DEATHTOUCH, Keyword.INFECT, Keyword.WITHER)) {
            if (gameQueryService.hasKeyword(gameData, creature, keyword)) {
                snapshot.getGrantedKeywords().add(keyword);
            }
        }
        return snapshot;
    }

    private record DealtDamageTriggerData(Card card, UUID permanentId, UUID controllerId, int damageDealt,
                                          Permanent sourceSnapshot,
                                          Card sourceCard, UUID sourcePermanentId, UUID sourceControllerId,
                                          List<List<Read>> dealtDamageAbilities,
                                          List<List<Read>> combatDamageReceivedAbilities) {}

    private List<DealtDamageTriggerData> collectDealtDamageTriggerData(GameData gameData, CombatDamageState state) {
        Map<UUID, DealtDamageTriggerData> triggersByDamagedPermanent = new LinkedHashMap<>();
        List<DealtDamageTriggerData> sourceSpecificTriggers = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (var entry : state.combatDamageDealtToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            Map<UUID, Integer> damageAmounts = state.combatDamageAmountsToCreatures.getOrDefault(source, Map.of());
            for (UUID targetId : entry.getValue()) {
                // Deduplicate: same source + target pair may appear twice (double strike phases)
                String key = source.getId() + ":" + targetId;
                if (!seen.add(key)) continue;

                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                Trigger dealtDamage = Triggers.dealtDamage();
                List<TriggeredAbility> abilities = damageTriggers.abilities(gameData, target, dealtDamage);
                // One list of reads for each ability (CR 603.3).
                List<List<Read>> dealtDamageAbilities =
                        new ArrayList<>(Read.readByAbility(abilities, dealtDamage));
                Trigger dealtCombatDamage = Triggers.dealtCombatDamage();
                List<TriggeredAbility> combatDamageReceived = damageTriggers.abilities(gameData, target,
                        dealtCombatDamage);
                List<List<Read>> combatDamageReceivedAbilities =
                        plainAbilities(combatDamageReceived, dealtCombatDamage);
                UUID controllerId = gameData.findControllerOf(target);
                if (controllerId == null) continue;
                int damageAmount = damageAmounts.getOrDefault(targetId, 0);
                // "Whenever a source deals damage to this" triggers once for each source (CR 603.2c).
                List<List<Read>> sourceSpecificAbilities = dealtDamageAbilities.stream()
                        .filter(ability -> ability.getFirst().eachSource())
                        .toList();
                dealtDamageAbilities.removeIf(ability -> ability.getFirst().eachSource());
                if (!sourceSpecificAbilities.isEmpty()) {
                    sourceSpecificTriggers.add(new DealtDamageTriggerData(
                            target.getCard(), target.getId(), controllerId, damageAmount,
                            lastKnownDamagedSnapshot(gameData, target),
                            source.getCard(), source.getId(), sourceControllerId,
                            sourceSpecificAbilities, List.of()));
                }
                if (dealtDamageAbilities.isEmpty() && combatDamageReceivedAbilities.isEmpty()) continue;
                DealtDamageTriggerData previous = triggersByDamagedPermanent.get(targetId);
                if (previous == null) {
                    triggersByDamagedPermanent.put(targetId, new DealtDamageTriggerData(
                            target.getCard(), target.getId(), controllerId, damageAmount,
                            lastKnownDamagedSnapshot(gameData, target),
                            source.getCard(), source.getId(), sourceControllerId,
                            dealtDamageAbilities, combatDamageReceivedAbilities));
                } else {
                    // The combat damage dealt to this permanent at once is one event (CR 603.2c); one dealt by
                    // several sources names no single "that source".
                    boolean sameSource = source.getId().equals(previous.sourcePermanentId());
                    boolean sameController = java.util.Objects.equals(sourceControllerId,
                            previous.sourceControllerId());
                    triggersByDamagedPermanent.put(targetId, new DealtDamageTriggerData(
                            previous.card(), previous.permanentId(), previous.controllerId(),
                            previous.damageDealt() + damageAmount, previous.sourceSnapshot(),
                            sameSource ? previous.sourceCard() : null,
                            sameSource ? previous.sourcePermanentId() : null,
                            sameController ? previous.sourceControllerId() : null,
                            previous.dealtDamageAbilities(), previous.combatDamageReceivedAbilities()));
                }
            }
        }
        sourceSpecificTriggers.addAll(triggersByDamagedPermanent.values());
        return sourceSpecificTriggers;
    }

    /**
     * The combat damage step's "whenever this is dealt damage" abilities, read as the damage was dealt, go on the stack
     * through the watch's default as noncombat damage's do ({@code TriggerCollectionService.collectDealtDamageAbilities}),
     * with the damaged permanent as it was dealt the damage: it may have died since.
     */
    private void processDealtDamageTriggers(GameData gameData, List<DealtDamageTriggerData> triggerData) {
        for (DealtDamageTriggerData data : triggerData) {
            if (data.dealtDamageAbilities().isEmpty()) continue;
            damageTriggers.collectDealtDamageAbilities(gameData,
                    data.dealtDamageAbilities(),
                    Triggers.dealtDamage(), data.sourceSnapshot(), data.controllerId(),
                    new TriggerContext.DamageToCreature(data.sourceSnapshot(), data.damageDealt(),
                            data.sourceControllerId(), data.sourceCard(), data.sourcePermanentId(),
                            data.sourceSnapshot()));
        }
    }

    private void processCombatDamageReceivedTriggers(GameData gameData,
                                                      List<DealtDamageTriggerData> triggerData) {
        for (DealtDamageTriggerData data : triggerData) {
            if (data.damageDealt() <= 0) continue;

            // Each ability is one object of all its effects (CR 603.3), its event condition and intervening "if"
            // judged once as it triggers, from the permanent as it was dealt the damage (CR 603.2, CR 603.4); a
            // declared target is one choice with every effect (CR 603.3d).
            ConditionContext context = ConditionContext.forPermanent(data.sourceSnapshot(), data.controllerId())
                    .withEventValue(data.damageDealt());
            for (List<Read> read : data.combatDamageReceivedAbilities()) {
                TriggeredAbility ability = read.getFirst().ability();
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                // "That much" is the damage dealt, the entry's event value, and the permanent as it was dealt the
                // damage its snapshot, whatever the effects are.
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        data.card(), data.sourceSnapshot(), data.controllerId(), data.card().getName() + "'s ability",
                        context, entry -> {
                            entry.setEventValue(data.damageDealt());
                            entry.setSourcePermanentSnapshot(new Permanent(data.sourceSnapshot()));
                        }));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(data.card()));
                }
            }
        }
    }

    private void checkCombatExcessDamageTriggers(GameData gameData, CombatDamageState state,
                                                 List<Permanent> atkBf, List<Permanent> defBf) {
        Set<UUID> processed = new HashSet<>();
        for (UUID permanentId : state.damageToPlaneswalkers.keySet()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isPlaneswalker(gameData, permanent)) continue;
            int damage = gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanentId, 0)
                    - state.damageDealtToPermanentsBeforeStep.getOrDefault(permanentId, 0);
            if (damage <= 0) continue;
            int lethalDamage = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, permanent)
                    ? state.toughnessBeforeStep.getOrDefault(permanentId, 0)
                    : state.loyaltyBeforeStep.getOrDefault(permanentId, 0);
            damageTriggers.checkOpponentPermanentDealtExcessDamageTriggers(
                    gameData, permanent, gameQueryService.findPermanentController(gameData, permanentId),
                    Math.max(0, damage - lethalDamage), false);
            processed.add(permanentId);
        }

        for (Permanent permanent : concatPermanents(atkBf, defBf)) {
            if (processed.contains(permanent.getId()) || !gameQueryService.isCreature(gameData, permanent)) continue;
            UUID permanentId = permanent.getId();
            int damage = gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanentId, 0)
                    - state.damageDealtToPermanentsBeforeStep.getOrDefault(permanentId, 0);
            if (damage <= 0) continue;
            int markedDamageBefore = state.markedDamageBeforeStep.getOrDefault(permanentId, 0);
            int lethalDamage = permanent.isDamagedByDeathtouch()
                    ? 1
                    : Math.max(0, state.lethalDamageThresholdBeforeStep.getOrDefault(permanentId,
                            gameQueryService.getLethalDamageThreshold(gameData, permanent)) - markedDamageBefore);
            int excessDamage = Math.max(0, damage - lethalDamage);
            if (excessDamage > 0) {
                gameData.recordExcessDamageToPermanent(permanentId);
            }
            damageTriggers.checkOpponentPermanentDealtExcessDamageTriggers(
                    gameData, permanent, gameQueryService.findPermanentController(gameData, permanentId),
                    excessDamage, false);
        }
    }

    private static List<Permanent> concatPermanents(List<Permanent> first, List<Permanent> second) {
        List<Permanent> result = new ArrayList<>(first.size() + second.size());
        result.addAll(first);
        result.addAll(second);
        return result;
    }

    private void processOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData,
                                                                           CombatDamageState state) {
        for (var entry : state.combatDamageRecipientControllers.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) {
                sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
            }
            damageTriggers.checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                    gameData, source.getCard(), sourceControllerId, source.getId(), entry.getValue());
        }
    }

    private void processCombatDamageToBattleTriggers(GameData gameData, CombatDamageState state) {
        Map<UUID, Map<Permanent, Integer>> damageByBattle = new LinkedHashMap<>();
        for (var sourceEntry : state.combatDamageAmountsToBattles.entrySet()) {
            for (var battleEntry : sourceEntry.getValue().entrySet()) {
                damageByBattle.computeIfAbsent(battleEntry.getKey(), ignored -> new LinkedHashMap<>())
                        .put(sourceEntry.getKey(), battleEntry.getValue());
            }
        }
        for (var battleEntry : damageByBattle.entrySet()) {
            Set<String> firedBatchedAllyAbilities = new HashSet<>();
            for (var sourceEntry : battleEntry.getValue().entrySet()) {
                UUID controllerId = state.combatDamageDealerControllers.get(sourceEntry.getKey());
                if (controllerId == null) controllerId = gameData.findControllerOf(sourceEntry.getKey());
                if (controllerId == null) continue;
                checkAllyCreatureCombatDamageToPlayerTriggers(
                        gameData, sourceEntry.getKey(), controllerId, battleEntry.getKey(),
                        sourceEntry.getValue(), battleEntry.getValue(), firedBatchedAllyAbilities, true, false);
            }
        }
    }

    private void snapshotDelayedCombatDamageDrawSources(GameData gameData, CombatDamageState state) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageDraw.class)) return;

        Map<UUID, Permanent> damageSources = new LinkedHashMap<>();
        for (Permanent creature : state.combatDamageDealtToPlayer.keySet()) {
            damageSources.put(creature.getId(), creature);
        }
        for (Permanent creature : state.combatDamageDealtToPlaneswalker.keySet()) {
            damageSources.put(creature.getId(), creature);
        }

        for (DelayedCombatDamageDraw delayed : gameData.getDelayedActions(DelayedCombatDamageDraw.class)) {
            Set<UUID> qualifyingSources = new LinkedHashSet<>();
            for (Permanent creature : damageSources.values()) {
                int playerDamage = state.combatDamageDealtToPlayer.getOrDefault(creature, 0);
                int planeswalkerDamage = state.combatDamageDealtToPlaneswalker.getOrDefault(creature, 0);
                if (playerDamage <= 0 && (!delayed.includesPlaneswalkers() || planeswalkerDamage <= 0)) continue;

                UUID controllerId = state.combatDamageDealerControllers.get(creature);
                if (controllerId == null) controllerId = gameData.findControllerOf(creature);
                if (!delayed.controllerId().equals(controllerId)) continue;
                if (delayed.sourcePredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, creature, delayed.sourcePredicate())) continue;
                qualifyingSources.add(creature.getId());
            }
            state.delayedCombatDamageDrawQualifications.add(
                    new CombatDamageState.DelayedCombatDamageDrawQualification(delayed, qualifyingSources));
        }
    }

    private void snapshotDelayedCombatDamageLookAtHandAndDrawSources(GameData gameData,
                                                                       CombatDamageState state,
                                                                       UUID damagedPlayerId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageLookAtHandAndDraw.class)) return;

        for (DelayedCombatDamageLookAtHandAndDraw delayed
                : gameData.getDelayedActions(DelayedCombatDamageLookAtHandAndDraw.class)) {
            boolean qualifyingCreatureDealtDamage = false;
            for (var damageEntry : state.combatDamageDealtToPlayer.entrySet()) {
                Permanent creature = damageEntry.getKey();
                if (damageEntry.getValue() <= 0) continue;

                UUID controllerId = state.combatDamageDealerControllers.get(creature);
                if (controllerId == null) controllerId = gameData.findControllerOf(creature);
                if (!delayed.controllerId().equals(controllerId)) continue;
                if (delayed.sourcePredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, creature, delayed.sourcePredicate())) continue;
                qualifyingCreatureDealtDamage = true;
                break;
            }
            if (qualifyingCreatureDealtDamage) {
                state.delayedCombatDamageLookAtHandAndDrawQualifications.add(
                        new CombatDamageState.DelayedCombatDamageLookAtHandAndDrawQualification(
                                delayed, damagedPlayerId));
            }
        }
    }

    /**
     * Triggers what {@code event}, a stage of a combat damage step, triggers: as the step's damage is dealt (CR 510.2),
     * before the state-based actions, the abilities that see it, those that wait on the stack while the state-based
     * actions run held in the step's state ({@code CombatDamageState.held…}) for the engine to put back above the
     * abilities those actions trigger; after them, the rest, read from the step's state as the damage was dealt.
     */
    public void trigger(GameData gameData, CombatDamageStepEvent event) {
        switch (event.stage()) {
            case DEALT -> dealt(gameData, event);
            case AFTER_STATE_BASED_ACTIONS -> afterStateBasedActions(gameData, event);
        }
    }

    /** What the abilities this step triggers read of the step as its damage was dealt, kept for the second stage. */
    private static final class Snapshot {
        private List<DealtDamageTriggerData> dealtDamageTriggerData = List.of();
        private Map<Permanent, List<TriggeredAbility>> damageToCreatureAbilities = Map.of();
        private Map<Permanent, UUID> attachedDamageSources = Map.of();
        private Map<UUID, Permanent> damagedCreatureSnapshots = Map.of();
        private Map<UUID, List<Permanent>> allyCombatDamageWatchers = Map.of();
        private Map<UUID, List<DamageTriggers.AllyDamageTriggerSnapshot>> allyDamageWatchers = Map.of();
        private final Map<UUID, List<Permanent>> battlefieldsBeforeStateBasedActions = new LinkedHashMap<>();
    }

    private void dealt(GameData gameData, CombatDamageStepEvent event) {
        CombatDamageState state = event.state();
        UUID activeId = event.activePlayerId();
        UUID defenderId = event.defendingPlayerId();
        List<Permanent> atkBf = gameData.playerBattlefields.getOrDefault(activeId, List.of());
        List<Permanent> defBf = defenderId == null ? List.of()
                : gameData.playerBattlefields.getOrDefault(defenderId, List.of());
        Snapshot snapshot = new Snapshot();
        state.combatTriggerSnapshot = snapshot;
        snapshot.dealtDamageTriggerData = collectDealtDamageTriggerData(gameData, state);
        checkCombatExcessDamageTriggers(gameData, state, atkBf, defBf);
        processAllyDealtDamageToPlaneswalkerTriggers(gameData, state);
        processOpponentSourceDamageToYouOrYourPermanentTriggers(gameData, state);
        checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers(gameData, state, defenderId);

        for (var entry : state.combatDamageDealt.entrySet()) {
            if (entry.getValue() > 0) {
                damageTriggers.queueEnchantedCreatureDealsDamageTriggers(
                        gameData, entry.getKey(), entry.getValue());
            }
        }

        for (var sourceEntry : state.combatDamageAmountsToCreatures.entrySet()) {
            for (var targetEntry : sourceEntry.getValue().entrySet()) {
                damageTriggers.queueEnchantedCreatureDealsDamageToCreatureTriggers(
                        gameData, sourceEntry.getKey(), targetEntry.getKey(), targetEntry.getValue());
            }
        }

        for (Permanent source : state.combatDamageDealt.keySet()) {
            state.selfDealsCombatDamageAbilities.put(source,
                    grantedCombatAbilities(gameData, source, Triggers.dealsCombatDamage()));
            state.selfDealsCombatDamageToPlayerOrPlaneswalkerAbilities.put(source, grantedCombatAbilities(
                    gameData, source, Triggers.dealsCombatDamageToPlayerOrPlaneswalker()));
            state.selfDealsCombatDamageToPlayerOrBattleAbilities.put(source, grantedCombatAbilities(
                    gameData, source, Triggers.dealsCombatDamageToPlayerOrBattle()));
        }

        Map<Permanent, List<TriggeredAbility>> damageToCreatureAbilities = new HashMap<>();
        for (Permanent source : state.combatDamageDealtToCreatures.keySet()) {
            damageToCreatureAbilities.put(source,
                    damageTriggers.abilities(gameData, source, Triggers.dealsCombatDamageToCreature()));
        }
        Map<Permanent, UUID> attachedDamageSources = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.isAttached()) {
                attachedDamageSources.put(new Permanent(permanent), controllerId);
            }
        });
        Map<UUID, Permanent> damagedCreatureSnapshots = snapshotCombatDamagedCreatures(gameData, state);
        // The damage abilities below trigger as the damage is dealt and go on the stack through the stack door now, so
        // they stay on the stack while state-based actions run: a creature they name that dies is stamped with its
        // last-known information there (CR 608.2h). After the state-based actions they are moved above the abilities
        // those actions triggered, in the order they were put.
        int beforeGlobalDamageTriggers = gameData.stack.size();
        processAnyCreatureDealtDamageTriggers(gameData, state, damagedCreatureSnapshots);
        List<StackEntry> globalDamageTriggers = new ArrayList<>(
                gameData.stack.subList(beforeGlobalDamageTriggers, gameData.stack.size()));
        snapshotDelayedCombatDamageDrawSources(gameData, state);
        snapshotDelayedCombatDamageLookAtHandAndDrawSources(gameData, state, defenderId);
        snapshotSelfDealsDamageAbilities(gameData, state);
        Map<UUID, List<Permanent>> allyCombatDamageWatchers =
                damageTriggers.snapshotAllyCreatureDealsCombatDamageWatchers(gameData);
        Map<UUID, List<com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers.AllyDamageTriggerSnapshot>> allyDamageWatchers =
                damageTriggers.snapshotAllyCreaturesDealDamageToOpponentTriggers(gameData);
        // "Whenever a [creature] deals damage" abilities trigger as the damage is dealt, so a watcher
        // that is destroyed by that same damage (a Sliver watching itself) must be scanned before
        // state-based actions remove it. The triggers are put on the stack after the other combat
        // damage triggers, as they were when this ran after the state-based actions. "Whenever damage is
        // dealt to a player" (Sower of Discord) triggers once for each player dealt combat damage in this
        // step, the damage of every source summed, scanned with them.
        int beforeSourceDamageReflections = gameData.stack.size();
        processSourceDealsDamageReflectionTriggers(gameData, state, defenderId, damagedCreatureSnapshots);
        combatDamageByPlayer(state, defenderId).forEach((playerId, damage) ->
                damageTriggers.checkPlayerDealtDamageTriggers(gameData, playerId, damage));
        List<StackEntry> sourceDamageReflectionTriggers = new ArrayList<>(
                gameData.stack.subList(beforeSourceDamageReflections, gameData.stack.size()));
        int beforeSelfAllyDamageReflections = gameData.stack.size();
        processSelfDealtDamageToCreatureReflectionTriggers(gameData, state, damagedCreatureSnapshots);
        List<StackEntry> selfAllyDamageReflectionTriggers = new ArrayList<>(
                gameData.stack.subList(beforeSelfAllyDamageReflections, gameData.stack.size()));
        int beforeWatcherAllyDamageReflections = gameData.stack.size();
        processWatchersDealtDamageToCreatureReflectionTriggers(gameData, state, damagedCreatureSnapshots);
        List<StackEntry> watcherAllyDamageReflectionTriggers = new ArrayList<>(
                gameData.stack.subList(beforeWatcherAllyDamageReflections, gameData.stack.size()));
        snapshot.damageToCreatureAbilities = damageToCreatureAbilities;
        snapshot.attachedDamageSources = attachedDamageSources;
        snapshot.damagedCreatureSnapshots = damagedCreatureSnapshots;
        snapshot.allyCombatDamageWatchers = allyCombatDamageWatchers;
        snapshot.allyDamageWatchers = allyDamageWatchers;
        state.heldGlobalDamageTriggers.addAll(globalDamageTriggers);
        state.heldSourceDamageReflectionTriggers.addAll(sourceDamageReflectionTriggers);
        state.heldSelfAllyDamageReflectionTriggers.addAll(selfAllyDamageReflectionTriggers);
        state.heldWatcherAllyDamageReflectionTriggers.addAll(watcherAllyDamageReflectionTriggers);
        gameData.playerBattlefields.forEach((playerId, battlefield) ->
                snapshot.battlefieldsBeforeStateBasedActions.put(playerId, new ArrayList<>(battlefield)));
    }

    private void afterStateBasedActions(GameData gameData, CombatDamageStepEvent event) {
        CombatDamageState state = event.state();
        UUID activeId = event.activePlayerId();
        UUID defenderId = event.defendingPlayerId();
        Snapshot snapshot = state.combatTriggerSnapshot instanceof Snapshot taken ? taken : new Snapshot();
        state.combatTriggerSnapshot = null;
        List<DealtDamageTriggerData> dealtDamageTriggerData = snapshot.dealtDamageTriggerData;
        Map<Permanent, List<TriggeredAbility>> damageToCreatureAbilities = snapshot.damageToCreatureAbilities;
        Map<Permanent, UUID> attachedDamageSources = snapshot.attachedDamageSources;
        Map<UUID, Permanent> damagedCreatureSnapshots = snapshot.damagedCreatureSnapshots;
        Map<UUID, List<Permanent>> allyCombatDamageWatchers = snapshot.allyCombatDamageWatchers;
        Map<UUID, List<DamageTriggers.AllyDamageTriggerSnapshot>> allyDamageWatchers = snapshot.allyDamageWatchers;
        List<StackEntry> watcherAllyDamageReflectionTriggers = state.heldWatcherAllyDamageReflectionTriggers;
        List<StackEntry> selfAllyDamageReflectionTriggers = state.heldSelfAllyDamageReflectionTriggers;
        Set<UUID> survivingIds = new HashSet<>();
        gameData.forEachPermanent((pid, p) -> survivingIds.add(p.getId()));
        Map<UUID, List<Permanent>> departedWatchersByController = new LinkedHashMap<>();
        snapshot.battlefieldsBeforeStateBasedActions.forEach((playerId, permanents) -> {
            for (Permanent permanent : permanents) {
                if (!survivingIds.contains(permanent.getId())) {
                    departedWatchersByController.computeIfAbsent(playerId, ignored -> new ArrayList<>())
                            .add(permanent);
                }
            }
        });

        processSelfDealsCombatDamageTriggers(gameData, state, defenderId, allyCombatDamageWatchers);
        processSelfDealsCombatDamageToPlayerOrPlaneswalkerTriggers(gameData, state);
        processSelfDealsCombatDamageToPlayerOrBattleTriggers(gameData, state);
        processCombatDamageToBattleTriggers(gameData, state);
        processCombatDamageToCreatureTriggers(gameData, state.combatDamageDealtToCreatures,
                state.combatDamageDealerControllers, state.combatDamageTargetControllers, damageToCreatureAbilities,
                state, damagedCreatureSnapshots);
        processCombatDamageToBlockingCreatureTriggers(gameData, state);

        // Acidic Dagger's delayed "destroy the non-Wall creature that creature damaged" trigger.
        processDelayedDestroyCreatureDamagedTriggers(gameData, state.combatDamageDealtToCreatures);

        processDelayedPlaneswalkerCombatDamageTriggers(gameData, state.combatDamageDealtToPlaneswalker);

        // Tamiyo, Field Researcher's +1: "until your next turn, whenever either of those creatures
        // deals combat damage, you draw a card".
        processDelayedWatchedCreatureCombatDamageTriggers(gameData, state);
        processDelayedNamedCreatureCombatDamageTriggers(gameData, state);

        // Process ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
        gameData.stack.addAll(watcherAllyDamageReflectionTriggers);
        processAllyDealtDamageToCreatureReflectionTriggers(gameData, state, damagedCreatureSnapshots);
        gameData.stack.addAll(selfAllyDamageReflectionTriggers);

        // Process ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul)
        processDealtDamageTriggers(gameData, dealtDamageTriggerData);

        // Process combat-damage-received triggers (e.g. Wall of Essence)
        processCombatDamageReceivedTriggers(gameData, dealtDamageTriggerData);



        // Process ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
        for (var entry : state.defDamageTaken.entrySet()) {
            if (entry.getValue() > 0) {
                damageTriggers.checkOpponentCreatureDealtDamageTriggers(gameData, defenderId);
            }
        }
        for (var entry : state.atkDamageTaken.entrySet()) {
            if (entry.getValue() > 0) {
                damageTriggers.checkOpponentCreatureDealtDamageTriggers(gameData, activeId);
            }
        }

        // A blocker can also deal combat damage to the defending player (Butcher Orgg).
        // Attribute triggers and damage history to each source's controller.
        Map<UUID, Map<UUID, Map<Permanent, Integer>>> playerDamageByRecipientAndController = new LinkedHashMap<>();
        state.combatDamageDealtToPlayer.forEach((source, damage) ->
                playerDamageByRecipientAndController.computeIfAbsent(
                        state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                        ignored -> new LinkedHashMap<>()).computeIfAbsent(
                        state.combatDamageDealerControllers.getOrDefault(source, activeId),
                        ignored -> new LinkedHashMap<>()).put(source, damage));
        state.redirectedCombatDamageToPlayers.forEach((recipientId, damageBySource) ->
                damageBySource.forEach((source, damage) ->
                        playerDamageByRecipientAndController.computeIfAbsent(
                                recipientId, ignored -> new LinkedHashMap<>()).computeIfAbsent(
                                state.combatDamageDealerControllers.getOrDefault(source, activeId),
                                ignored -> new LinkedHashMap<>()).merge(source, damage, Integer::sum)));
        // Sources of "whenever your opponents are dealt combat damage" (one or more creatures, any opponents) that
        // already triggered in this combat damage step: once for the step however many players were dealt damage.
        Set<String> firedOncePerStepAbilities = new HashSet<>();
        playerDamageByRecipientAndController.forEach((recipientId, damageByController) ->
                damageByController.forEach((controllerId, damageBySource) -> {
                    processCombatDamageToPlayerTriggers(gameData, damageBySource, controllerId, recipientId,
                            attachedDamageSources, allyDamageWatchers.getOrDefault(controllerId, List.of()),
                            departedWatchersByController, firedOncePerStepAbilities);
                    damageTriggers.checkEmblemCombatDamageTriggers(
                            gameData, controllerId, recipientId, damageBySource);
                    processDelayedCombatDamageLootTriggers(gameData, damageBySource, controllerId);
                    processDelayedCombatDamageTokenTriggers(gameData, damageBySource, controllerId, recipientId);
                }));
        processDelayedCombatDamageBecomeMonarchTriggers(gameData, state, activeId);

        processDelayedCombatDamageLookAtHandAndDrawTriggers(gameData, state);

        processDelayedCombatDamageEffects(gameData, state);

        // Process delayed combat damage draw triggers (e.g. Flitterwing Nuisance's ability)
        processDelayedCombatDamageDrawTriggers(gameData, state);

        Map<UUID, Set<String>> firedBatchedTriggerKeysByRecipient = new HashMap<>();
        playerDamageByRecipientAndController.forEach((recipientId, damageByController) -> {
            Map<Permanent, Integer> damageBySource = new LinkedHashMap<>();
            damageByController.values().forEach(damageBySource::putAll);
            processCombatDamageReflectionTriggers(gameData, damageBySource, activeId, recipientId);
            Set<String> firedBatchedTriggerKeys = firedBatchedTriggerKeysByRecipient.computeIfAbsent(
                    recipientId, ignored -> new HashSet<>());
            damageBySource.forEach((source, amount) -> {
                if (amount <= 0) return;
                damageTriggers.checkDamageDealtToControllerTriggers(
                        gameData, recipientId, source.getId(), true, firedBatchedTriggerKeys);
                damageTriggers.checkEnchantedCreatureDealtDamageToControllerReflectTriggers(
                        gameData, recipientId, source.getId(), amount);
                UUID sourceController = state.combatDamageDealerControllers.getOrDefault(source, activeId);
                damageTriggers.checkAllySourceDealtDamageToOpponentTriggers(
                        gameData, recipientId, sourceController, source.getId(), amount);
                damageTriggers.checkCreatureDamageToYouOrYourPermanentTriggers(
                        gameData, recipientId, null, source, amount);
            });
            int totalDamage = damageBySource.values().stream().mapToInt(Integer::intValue).sum();
            damageTriggers.checkControllerDealtDamageTriggers(
                    gameData, recipientId, null, totalDamage);
            damageByController.forEach((controllerId, sources) -> {
                if (controllerId.equals(recipientId)) {
                    damageTriggers.checkControllerDealtDamageTriggers(
                            gameData, recipientId, controllerId,
                            sources.values().stream().mapToInt(Integer::intValue).sum(), false);
                    return;
                }
                sources.values().forEach(amount ->
                        damageTriggers.checkControllerDealtDamageTriggers(
                                gameData, recipientId, controllerId, amount, false));
            });
        });
        // ON_ANY_SOURCE_DEALS_DAMAGE reflection triggers (e.g. Justice) were collected before the
        // state-based actions; combatDamageDealt is already summed per source for this step, matching
        // the "add up all the damage" ruling.
        gameData.stack.addAll(state.heldSourceDamageReflectionTriggers);

    }
}
