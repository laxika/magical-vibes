package com.github.laxika.magicalvibes.service.trigger.pipeline;

import org.springframework.stereotype.Service;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.TargetEvent;
import com.github.laxika.magicalvibes.model.trigger.TargetTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

/**
 * Triggers the abilities of a {@link TargetEvent}: the players and permanents a spell or ability targets became its
 * targets (CR 601.2c, 602.2b, 603.3d), or a player chose its targets. Each becomes-the-target ability is read by its
 * target trigger condition ({@link TargetTrigger}: the further restriction on the permanent that becomes the target,
 * the narrower event "a spell [or ability] you control", the targeting source, the limit) and handed whole to the
 * stack door, which judges its intervening "if" (CR 603.4) and limit, counts the additional times it triggers and
 * chooses its targets as it goes on the stack (CR 603.3d); the record names the spell or ability that targeted as the
 * triggering spell and its controller as the triggering player. The abilities of one event trigger together, under one
 * query scope: what they write isn't read by the layered board.
 */
@Slf4j
@Service
public class TargetTriggers {

    /** A record that names nothing beyond what the request's own fields name. */
    private static final TriggerRecord NO_EXTRA_RECORD = entry -> {
    };

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    public TargetTriggers(@Lazy GameQueryService gameQueryService,
                          @Lazy PredicateEvaluationService predicateEvaluationService,
                          @Lazy ConditionEvaluationService conditionEvaluationService,
                          @Lazy GameLogService gameLogService,
                          @Lazy GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport,
                          @Lazy TriggeredAbilityStackService triggeredAbilityStackService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
        this.grantedTriggeredAbilitySupport = grantedTriggeredAbilitySupport;
        this.triggeredAbilityStackService = triggeredAbilityStackService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, TargetEvent event) {
        StackEntry targeting = event.targeting();
        switch (event.kind()) {
            case BECAME_TARGET_OF_SPELL -> gameQueryService.withQueryScope(gameData, () -> {
                collectBecomesTargetOfSpellTriggers(gameData, targeting);
                return null;
            });
            case BECAME_TARGET_OF_ABILITY -> gameQueryService.withQueryScope(gameData, () -> {
                collectBecomesTargetOfAbilityTriggers(gameData, targeting);
                return null;
            });
            case TARGETS_CHOSEN -> checkTargetChoiceTriggers(gameData, targeting);
        }
    }

    /** The abilities of {@code card} with a trigger condition watched as {@code watched} on the battlefield. */
    private static List<TriggeredAbility> printed(Card card, Trigger watched) {
        return WatchedAbilities.printed(card, watched, Zone.BATTLEFIELD);
    }

    private StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    /**
     * The ability a mannequin counter gives its permanent while Makeshift Mannequin's grant lasts: "When this creature
     * becomes the target of a spell or ability, sacrifice it."
     */
    private static final TriggeredAbility MANNEQUIN_ABILITY = new TriggeredAbility(
            Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());

    /**
     * The ward {2} of a cloaked permanent (CR 701.58a): "Whenever this permanent becomes the target of a spell or
     * ability an opponent controls, counter that spell or ability unless that player pays {2}" (CR 702.21a).
     */
    private static final TriggeredAbility CLOAKED_WARD_ABILITY = new TriggeredAbility(
            Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

    /**
     * Detects every becomes-the-target trigger of one spell's targeting event. All of them trigger
     * on the same event (CR 603.2) and only go on the stack afterwards (CR 603.3), so the caller runs
     * this under one query scope: the collectors' per-permanent layered queries share a single board
     * instead of each re-fingerprinting it, which would cost targets × collectors × battlefield size
     * board hashes. What the collectors write — triggered abilities on the stack, log lines, queued
     * choices and may abilities — is not read by the layered board or the static-bonus assembly.
     */
    private void collectBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        if (spellEntry.getTargetZone() == Zone.STACK) {
            if (spellEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, spellEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
            } else {
                for (UUID targetId : spellEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetId());
        }
        if (spellEntry.getTargetIds() != null) {
            targetIds.addAll(spellEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetsWithAllyTrigger = new HashSet<>();
        Set<List<Object>> collectedAllyTargetTriggers = new HashSet<>();

        for (UUID targetId : new java.util.LinkedHashSet<>(targetIds)) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                            gameData, targetId, null, spellEntry, collectedAllyTargetTriggers);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfSpellTriggers(gameData, targetId, spellEntry);
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            if (targetsWithAllyTrigger.add(targetId)) {
                collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                                gameData, controllerId, targetPermanent, spellEntry,
                                collectedAllyTargetTriggers);
            }
            collectAllyPermanentBecomesTargetOfOpponentTriggers(
                            gameData, controllerId, targetPermanent, spellEntry);
            collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfOpponentSpellTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, spellEntry);
            collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfSpellTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead — their triggers
            // monitor the enchanted/equipped creature, not themselves (Spectral Prison is not
            // sacrificed when a spell targets the Aura rather than the enchanted creature).
            if (targetPermanent.isAttached() && (targetPermanent.getCard().getKeywords().contains(Keyword.WARD)
                    || hasGrantedCounterUnlessTrigger(gameData, targetPermanent))) {
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
            }
            // A bestowed permanent (Crystalline Nautilus) is still "this permanent" while attached as an Aura, so its
            // own printed becomes-target abilities apply to it, unlike an ordinary Aura's (see the loop below).
            if (controllerId != null && (!targetPermanent.isAttached() || targetPermanent.isBestow())) {
                collectBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
            }
            if (!targetPermanent.isAttached()) {
                collectBecomesTargetTriggers(
                                gameData, targetPermanent, controllerId, targetPermanent, spellEntry);
                collectBecomesTargetOfAuraSpellTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOnlyTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: the triggered ability is controlled by the controller of the
                        // permanent that has it (the aura/equipment), not the enchanted creature —
                        // the two differ when an Aura like Spectral Prison enchants an opponent's creature.
                        // A bestowed permanent's printed becomes-target abilities refer to itself, so they do not
                        // fire when the creature it enchants is targeted; only its granted ability does (on that creature).
                        if (!attached.isBestow()) {
                            collectBecomesTargetOfSpellOrAbilityTriggers(
                                            gameData, attached, playerId, spellEntry);
                        }
                        if (playerId.equals(controllerId)) {
                            collectBecomesTargetTriggers(
                                            gameData, attached, playerId, targetPermanent, spellEntry);
                            collectBecomesTargetOfAuraSpellTriggers(
                                            gameData, attached, playerId, spellEntry);
                            collectBecomesTargetOfOpponentCounterTriggers(
                                            gameData, attached, playerId, spellEntry);
                        } else {
                            collectBecomesTargetTriggers(gameData, attached, playerId, targetPermanent, spellEntry);
                            collectBecomesTargetOfAuraSpellTriggers(gameData, attached, playerId, spellEntry);
                            collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, spellEntry);
                        }
                    }
                }
            }
        }
    }

    /**
     * The ability counterpart of {@link #collectBecomesTargetOfSpellTriggers}, run under one query
     * scope for the same reason.
     */
    private void collectBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry.getTargetZone() == Zone.STACK && !abilityEntry.isNonTargeting()) {
            if (abilityEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, abilityEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
            } else {
                for (UUID targetId : abilityEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (abilityEntry.getTargetId() != null
                && (abilityEntry.getTargetZone() == null || abilityEntry.getTargetZone() == Zone.BATTLEFIELD)
                && !abilityEntry.isNonTargeting()) {
            targetIds.add(abilityEntry.getTargetId());
        }
        if (abilityEntry.getTargetIds() != null) {
            targetIds.addAll(abilityEntry.getTargetIds());
        }

        if (!abilityEntry.isNonTargeting()) {
            for (UUID permanentId : abilityEntry.getMixedZoneTargetPermanentIds().values()) {
                if (!targetIds.contains(permanentId)) targetIds.add(permanentId);
            }
        }

        collectAllyCreatureBecomesTargetOfActivatedAbilityTriggers(gameData, abilityEntry, targetIds);

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetsWithAllyTrigger = new HashSet<>();
        Set<List<Object>> collectedAllyTargetTriggers = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                            gameData, targetId, null, abilityEntry, collectedAllyTargetTriggers);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

            if (controllerId != null) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                                    gameData, controllerId, targetPermanent, abilityEntry,
                                    collectedAllyTargetTriggers);
                }
                collectAllyPermanentBecomesTargetOfOpponentTriggers(
                                gameData, controllerId, targetPermanent, abilityEntry);
                collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }

            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead.
            if (targetPermanent.isAttached() && controllerId != null
                    && targetPermanent.getCard().getKeywords().contains(Keyword.WARD)) {
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }
            if (controllerId != null && (!targetPermanent.isAttached() || targetPermanent.isBestow())) {
                collectBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }
            if (!targetPermanent.isAttached() && controllerId != null) {
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }

            // Check for "whenever a creature you control becomes the target of opponent's spell or ability"
            if (controllerId != null) {
                collectAllyCreatureBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }

            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, abilityEntry);

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: triggered ability controlled by the aura/equipment's controller. A bestowed
                        // permanent's printed abilities refer to itself, so they do not fire for the creature it enchants.
                        if (!attached.isBestow()) {
                            collectBecomesTargetOfSpellOrAbilityTriggers(
                                            gameData, attached, playerId, abilityEntry);
                        }
                        if (playerId.equals(controllerId)) {
                            collectBecomesTargetOfOpponentCounterTriggers(
                                            gameData, attached, playerId, abilityEntry);
                        } else {
                            collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, abilityEntry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Collects "whenever one or more creatures you control become the target of an activated ability" (Professor Hojo)
     * once for each ability that targets one or more of a player's creatures, however many it targets: each watching
     * ability goes to the stack door once, its "this ability triggers only once each turn" judged there.
     */
    private void collectAllyCreatureBecomesTargetOfActivatedAbilityTriggers(
            GameData gameData, StackEntry abilityEntry, List<UUID> targetIds) {
        if (abilityEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                || abilityEntry.isCopy()) {
            return;
        }

        Set<UUID> targetedCreatureControllers = new HashSet<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) continue;
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                targetedCreatureControllers.add(controllerId);
            }
        }

        for (UUID controllerId : targetedCreatureControllers) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent source : List.copyOf(battlefield)) {
                if (source.isCloaked() || source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                Trigger watch = Triggers.creaturesYouControlBecomeTargetOfActivatedAbility();
                for (TargetEffect read : targetReads(printed(source.getCard(), watch), watch)) {
                    requireNoFilter(read, watch);
                    requireWatchedEvent(read, watch);
                    putTargetAbility(gameData, read.ability(), source, controllerId, "'s ability", abilityEntry,
                            null, controllerId, NO_EXTRA_RECORD);
                }
            }
        }
    }

    /**
     * Checks all permanents controlled by the targeted player for abilities that watch that player or
     * one of their permanents becoming the target of an opponent's spell or ability, each handed to the stack door
     * whole with its filter judged once. "You or a permanent you control becomes the target" triggers once for each
     * such target (CR 603.2c; Rayne, Academy Chancellor's, Leovold's and Unsettled Mariner's rulings); "you and/or at
     * least one permanent you control becomes the target" ({@code TriggerMultiplicity.ONE_OR_MORE}, Leyline of
     * Combustion) once for the spell or ability, however many of them it targets: {@code collectedTriggers} holds the
     * abilities that already triggered for it. The record names the spell or ability's controller as "that player".
     */
    private void collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, Permanent targetPermanent,
            StackEntry triggeringEntry, Set<List<Object>> collectedTriggers) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        UUID spellControllerId = triggeringEntry.getControllerId();
        for (Permanent source : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            Trigger watch = Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility();
            List<TriggeredAbility> abilities = new ArrayList<>(printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));
            List<TargetEffect> reads = targetReads(abilities, watch);
            for (int abilityIndex = 0; abilityIndex < reads.size(); abilityIndex++) {
                TargetEffect read = reads.get(abilityIndex);
                requireWatchedEvent(read, watch);
                requireNoLimit(read, watch);
                // The filter on the permanent (Fey Steed's "creature or planeswalker"), which a targeted player fails.
                if (!targetFilterMatches(gameData, source, targetControllerId, targetPermanent, read)) continue;
                if (read.oneOrMore() && !collectedTriggers.add(List.of(source.getId(), abilityIndex))) continue;
                putTargetAbility(gameData, read.ability(), source, targetControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, targetControllerId, entry -> {
                            entry.setTargetId(spellControllerId);
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        });
            }
        }
    }

    /**
     * The first read of each becomes-the-target ability among {@code abilities} that {@code watch} reads, in order: the
     * ability with the trigger condition it is read by (its filter on the permanent, narrower event, source and limit),
     * which its collector judges before it hands the ability to the stack door.
     */
    private static List<TargetEffect> targetReads(List<TriggeredAbility> abilities, Trigger watch) {
        return readByAbility(abilities, watch).stream().map(List::getFirst).toList();
    }

    /**
     * The abilities among {@code abilities} that {@code watch} reads, for a collector that judges no filter, narrower
     * event or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    private static List<TriggeredAbility> unjudgedTargetAbilities(List<TriggeredAbility> abilities, Trigger watch) {
        List<TriggeredAbility> read = new ArrayList<>();
        for (TargetEffect first : targetReads(abilities, watch)) {
            requireNothingToJudge(first, watch);
            read.add(first.ability());
        }
        return read;
    }

    /**
     * The abilities {@code watch} reads of {@code source} for a collector that judges no filter, narrower event or
     * limit: its printed ones when {@code printed}, then those other permanents' static abilities grant it.
     */
    private List<TriggeredAbility> targetAbilities(GameData gameData, Permanent source, Trigger watch,
                                                   boolean printed) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (printed) {
            abilities.addAll(printed(source.getCard(), watch));
        }
        abilities.addAll(grantedTargetAbilities(gameData, source, watch));
        return unjudgedTargetAbilities(abilities, watch);
    }

    /**
     * Hands {@code ability}, a becomes-the-target ability of {@code source} controlled by {@code controllerId}, to the
     * stack door for {@code targetingEntry}, the spell or ability that targeted {@code target} (a permanent
     * {@code targetControllerId} controls, or {@code null} for a player). Its event condition is judged now from its
     * source (CR 603.2); the door judges its intervening "if" (CR 603.4) and limit, counts the additional times it
     * triggers (Valiant Emberkin's, for a creature you control becoming the target), chooses its targets as it goes on
     * the stack (CR 603.3d) and aims an effect that acts on "that spell or ability" at it. The record names the spell or
     * ability as the triggering spell and its controller as the triggering player, whatever the effects are, and
     * {@code extra} adds what the collector's event names besides.
     *
     * @return whether it triggered
     */
    private boolean putTargetAbility(GameData gameData, TriggeredAbility ability, Permanent source, UUID controllerId,
                                     String description, StackEntry targetingEntry, Permanent target,
                                     UUID targetControllerId, TriggerRecord extra) {
        ConditionContext context = ConditionContext.forPermanent(source, controllerId)
                .withTriggeringSpell(targetingEntry);
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        TriggerRecord record = entry -> {
            entry.setNonTargeting(true);
            recordTargetingObject(entry, targetingEntry);
            extra.stamp(entry);
        };
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                source.getCard(), source, controllerId, source.getCard().getName() + description, context, record)
                .withEvent(new TriggerContext.BecomesTarget(target, targetControllerId)));
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        }
        return triggered;
    }

    private void collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.getId().equals(targetPermanent.getId())) continue;

            Trigger watch = Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility();
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, targetControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, targetControllerId, entry -> {
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                            entry.setTriggeringPermanentControllerId(targetControllerId);
                        });
            }
        }
    }

    private void collectAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, Permanent targetPermanent,
            StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            Trigger watch = Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility();
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, targetControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, targetControllerId,
                        entry -> entry.setSourcePermanentSnapshot(new Permanent(source)));
            }
        }
    }

    /**
     * Records the event of a becomes-the-target ability on {@code entry}, whatever its effects are: the spell or ability
     * that targeted as its triggering spell (as it was then, and as it last existed on the stack once it has left,
     * CR 608.2h) and that object's controller as its triggering player.
     */
    private static void recordTargetingObject(StackEntry entry, StackEntry targetingEntry) {
        entry.setTriggeringCardId(targetingEntry.getTargetableId());
        entry.setTriggeringSpell(targetingEntry);
        entry.setTriggeringPlayerId(targetingEntry.getControllerId());
    }

    /**
     * Whether {@code ability} acts on "that spell or ability": one of its effects acts only on a spell or ability on the
     * stack ("counter it unless that player pays", ward). The ability goes on the stack whole either way (CR 603.3).
     */
    private static boolean actsOnASpell(TriggeredAbility ability) {
        return ability.effects().stream().map(EffectRegistration::effect)
                .anyMatch(TargetTriggers::actsOnASpell);
    }

    /** Whether {@code effect} acts only on a spell or ability on the stack ("counter that spell or ability"). */
    private static boolean actsOnASpell(CardEffect effect) {
        TargetSpec spec = effect.targetSpec();
        return spec.admits(TargetPredicate.Kind.SPELL) && !spec.admits(TargetPredicate.Kind.PERMANENT)
                && !spec.admits(TargetPredicate.Kind.PLAYER);
    }

    /**
     * "Whenever a player chooses one or more targets" (Grip of Chaos, Psychic Battle) and "whenever you or a permanent
     * becomes the target of an ability you control" (Loki, God of Mischief): each ability goes to the stack door whole,
     * its intervening "if" ("if it has a single target") judged there on the spell or ability whose targets were
     * chosen, its record naming that object as its triggering spell.
     */
    private void checkTargetChoiceTriggers(GameData gameData, StackEntry targetEntry) {
        if (targetEntry == null || !targetEntry.hasAnyTarget() || targetEntry.isNonTargeting()) {
            return;
        }

        Trigger watch = Triggers.playerChoosesTargets();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) {
                return;
            }
            for (TargetEffect read : targetReads(printed(permanent.getCard(), watch), watch)) {
                requireNoFilter(read, watch);
                if (read.byController()) {
                    // "Whenever you or a permanent becomes the target of an ability you control" (Loki).
                    if ((targetEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                            && targetEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                            || !controllerId.equals(targetEntry.getControllerId())
                            || !targetsPlayerOrPermanent(gameData, targetEntry)) {
                        continue;
                    }
                } else {
                    requireNoLimit(read, watch);
                }
                putTargetAbility(gameData, read.ability(), permanent, controllerId, "'s ability", targetEntry,
                        null, null, NO_EXTRA_RECORD);
            }
        });
    }


    private boolean targetsPlayerOrPermanent(GameData gameData, StackEntry targetEntry) {
        if (targetEntry.getTargetZone() == null
                && isPlayerOrPermanentTarget(gameData, targetEntry.getTargetId())) {
            return true;
        }
        return targetEntry.getTargetIds().stream()
                .anyMatch(targetId -> isPlayerOrPermanentTarget(gameData, targetId));
    }

    private boolean isPlayerOrPermanentTarget(GameData gameData, UUID targetId) {
        return targetId != null
                && (gameData.playerIds.contains(targetId)
                || gameQueryService.findPermanentById(gameData, targetId) != null);
    }

    private void collectBecomesTargetTriggers(GameData gameData, Permanent source, UUID controllerId,
                                              Permanent targetedCreature, StackEntry spellEntry) {
        if (source.isCloaked() || gameQueryService.hasLostPrintedAbilities(gameData, source)) return;
        Trigger watch = Triggers.becomesTargetOfSpell();
        List<TriggeredAbility> abilities = new ArrayList<>(printed(source.getCard(), watch));
        if (source == targetedCreature) {
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));
        }
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetedCreature.getId());

        // Each ability goes to the stack door whole: one with a target (Livewire Lash's "any target", granted to the
        // equipped creature, which is its source) has it chosen as it goes on the stack (CR 603.3d).
        for (TargetEffect read : targetReads(abilities, watch)) {
            requireNoFilter(read, watch);
            requireNoLimit(read, watch);
            // "This becomes the target of a spell you control" (Stormchaser Drake).
            if (read.byController() && !controllerId.equals(spellEntry.getControllerId())) continue;
            putTargetAbility(gameData, read.ability(), source, controllerId, "'s triggered ability", spellEntry,
                    targetedCreature, targetControllerId,
                    entry -> entry.setSourcePermanentSnapshot(new Permanent(source)));
        }
    }


    private void collectBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Every targeting of a permanent counts toward "for the first time each turn", including targetings
        // that happened before a granting source entered the battlefield.
        boolean targetedEarlierThisTurn = source.isBecomeTargetCounterUsedThisTurn();
        source.setBecomeTargetCounterUsedThisTurn(true);
        // A face-down cloaked permanent has no printed abilities, but abilities granted to it still apply.
        Trigger watch = Triggers.becomesTargetOfSpellOrAbility();
        List<TriggeredAbility> abilities = new ArrayList<>(source.isCloaked()
                || gameQueryService.hasLostPrintedAbilities(gameData, source)
                ? List.of()
                : printed(source.getCard(), watch));
        abilities.addAll(WatchedAbilities.temporary(source, watch));
        abilities.addAll(WatchedAbilities.persistent(source, watch));
        // Dismiss into Dream continuously grants "When this creature becomes the target of a spell
        // or ability, sacrifice it" to each creature its controller's opponents control.
        abilities.addAll(grantedTargetAbilities(gameData, source, watch));
        // Makeshift Mannequin: while a permanent has a mannequin counter, it has "When this creature
        // becomes the target of a spell or ability, sacrifice it." The grant comes from the resolving
        // spell (CR 611.2b), so it is tracked on the permanent rather than read from the counter alone.
        if (source.isMannequinGrantActive() && source.getCounterCount(CounterType.MANNEQUIN) > 0
                && !gameQueryService.hasLostPrintedAbilities(gameData, source)) {
            abilities.add(MANNEQUIN_ABILITY);
        }
        // The permanent that became the target: the enchanted or equipped creature for an attachment's ability ("that
        // creature", Fractured Loyalty), else the source itself.
        UUID targetedId = source.isAttached() && !source.isBestow() ? source.getAttachedTo() : source.getId();
        Permanent targeted = targetedId.equals(source.getId()) ? source
                : gameQueryService.findPermanentById(gameData, targetedId);
        UUID targetedControllerId = targeted == null ? null
                : gameQueryService.findPermanentController(gameData, targeted.getId());

        // Each ability goes to the stack door whole, separate abilities separate objects (CR 603.3b); the door judges
        // "triggers only once each turn" and marks it, the collector "for the first time each turn".
        for (TargetEffect first : targetReads(abilities, watch)) {
            requireNoFilter(first, watch);
            // "Whenever this becomes the target of a spell or ability you control" (valiant).
            if (first.byController() && !controllerId.equals(triggeringEntry.getControllerId())) continue;
            // "Becomes the target of a spell or ability for the first time each turn" (Glyph Keeper): any earlier
            // targeting this turn counts, whether or not the permanent had the ability then.
            if (first.limit() == TriggerLimit.FIRST_TIME_EACH_TURN && targetedEarlierThisTurn) continue;
            putTargetAbility(gameData, first.ability(), source, controllerId, "'s triggered ability",
                    triggeringEntry, targeted, targetedControllerId, entry -> {
                        if (source.isAttached()) {
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        }
                        entry.setTriggeringPermanentId(targetedId);
                    });
        }
    }


    /**
     * "Whenever this creature becomes the target of an Aura spell, &lt;effect&gt;." Narrower than
     * {@code Triggers.becomesTargetOfSpell()}: only Aura spells count, and there is no controller
     * restriction, so an opponent's Aura triggers it just as the controller's own does. Used by
     * Fugitive Druid.
     */
    private void collectBecomesTargetOfAuraSpellTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (source.isCloaked()) return;
        Card spellCard = spellEntry.getTargetingCard();
        if (spellCard == null || !spellCard.isAura()) return;

        Trigger watch = Triggers.becomesTargetOfAuraSpell();
        for (TriggeredAbility ability : unjudgedTargetAbilities(printed(source.getCard(), watch),
                watch)) {
            putTargetAbility(gameData, ability, source, controllerId, "'s triggered ability", spellEntry, source,
                    controllerId, NO_EXTRA_RECORD);
        }
    }


    private void collectBecomesTargetOfOpponentCounterTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(triggeringEntry.getControllerId())) return;

        boolean wardSuppressed = gameQueryService.preventsOpponentCreatureWardTriggers(
                gameData, triggeringEntry.getControllerId())
                && hasWardAbility(gameData, source);
        if (source.isFaceDown()
                && (source.isCloaked() || source.isFaceDownWard())
                && gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            if (wardSuppressed) return;
            enqueueCloakedWardTrigger(gameData, source, controllerId, triggeringEntry);
        }

        Trigger watch = Triggers.becomesTargetOfOpponentSpellOrAbility();
        // Each ability whole (CR 603.3): the source's own (printed, and granted to it for a duration), then those
        // other permanents' static abilities grant it.
        List<TriggeredAbility> abilities = new ArrayList<>();
        boolean sourceIsTarget = source.getId().equals(triggeringEntry.getTargetId())
                || source.getCard().getId().equals(triggeringEntry.getTargetId())
                || triggeringEntry.getDeclaredTargetIds().contains(source.getId())
                || triggeringEntry.getDeclaredTargetIds().contains(source.getCard().getId())
                || triggeringEntry.getMixedZoneTargetPermanentIds().containsValue(source.getId());
        if (!source.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, source)
                && (sourceIsTarget || !source.getCard().getKeywords().contains(Keyword.WARD))) {
            abilities.addAll(unjudgedTargetAbilities(printed(source.getCard(), watch), watch));
        }
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.temporary(source, watch), watch));
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.persistent(source, watch), watch));
        // Granted ward ("each legendary permanent you control has ward {2}") is a "this permanent becomes
        // the target" trigger, so an attached permanent only gets it when it is itself the target.
        List<TriggeredAbility> grantedAbilities = new ArrayList<>(
                unjudgedTargetAbilities(grantedTargetAbilities(gameData, source, watch), watch).stream()
                        .filter(ability -> sourceIsTarget || !actsOnASpell(ability))
                        .toList());
        boolean printedWardRemoved = source.getCard().getKeywords().contains(Keyword.WARD)
                && !gameQueryService.hasKeyword(gameData, source, Keyword.WARD);
        if (printedWardRemoved) {
            // A printed ward whose keyword was removed (e.g. "permanents your opponents control lose ward")
            // no longer has its counter-unless trigger.
            abilities.removeIf(TargetTriggers::actsOnASpell);
            grantedAbilities.removeIf(TargetTriggers::actsOnASpell);
        }
        if (wardSuppressed) {
            // Ward suppression only removes ward: a printed ward keyword's counter-unless goes, and so
            // does a granted ward, but a creature's own non-ward "counter unless" trigger stays (CR 702.21a).
            if (gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
                abilities.removeIf(TargetTriggers::actsOnASpell);
            }
            grantedAbilities.removeIf(TargetTriggers::actsOnASpell);
        }
        abilities.addAll(grantedAbilities);

        // The abilities that act on "that spell or ability" (ward and the other "counter it unless" abilities), each
        // handed to the stack door whole, which aims it at that spell or ability; the others of the watch are
        // collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers's.
        Permanent targeted = sourceIsTarget || !source.isAttached() ? source
                : gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        UUID targetedControllerId = targeted == null ? null
                : gameQueryService.findPermanentController(gameData, targeted.getId());
        for (TriggeredAbility ability : abilities) {
            if (!actsOnASpell(ability)) continue;
            putTargetAbility(gameData, ability, source, controllerId, "'s ability", triggeringEntry, targeted,
                    targetedControllerId, entry -> entry.setSourcePermanentSnapshot(new Permanent(source)));
        }
    }


    /**
     * Hands a face-down cloaked permanent's ward {2} (CR 701.58a, 702.21a) to the stack door, which aims it at the
     * spell or ability that targeted it, its triggering spell, which it counters unless that player pays. Its source is
     * a placeholder that names no face-down characteristic.
     */
    private void enqueueCloakedWardTrigger(GameData gameData, Permanent source, UUID controllerId,
                                           StackEntry triggeringEntry) {
        Card wardSourceCard = Card.namedRuntimePlaceholder("Cloaked creature");
        Permanent snapshot = new Permanent(source);
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(CLOAKED_WARD_ABILITY, wardSourceCard,
                source, controllerId, wardSourceCard.getName() + "'s ward ability",
                ConditionContext.forPermanent(source, controllerId).withTriggeringSpell(triggeringEntry), entry -> {
                    entry.setSourcePermanentSnapshot(snapshot);
                    recordTargetingObject(entry, triggeringEntry);
                })
                .withEvent(new TriggerContext.BecomesTarget(source, controllerId)));
    }


    private void collectControllerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        if (playerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        Trigger watch = Triggers.youBecomeTargetOfOpponentSpellOrAbility();
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (TriggeredAbility ability : unjudgedTargetAbilities(printed(source.getCard(),
                    watch), watch)) {
                // This triggered while the targeting ability was being put on the stack, so it goes on the
                // stack after that batch rather than being APNAP-ordered with it (CR 603.3b).
                putTargetAbility(gameData, ability, source, playerId, "'s triggered ability", triggeringEntry, null,
                        playerId, entry -> {
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                            entry.setTriggerOrderChosen(true);
                        });
            }
        }
    }


    private void collectControllerBecomesTargetOfSpellTriggers(
            GameData gameData, UUID playerId, StackEntry spellEntry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : targetAbilities(gameData, source,
                    Triggers.youBecomeTargetOfSpell(),
                    !gameQueryService.hasLostPrintedAbilities(gameData, source))) {
                putTargetAbility(gameData, ability, source, playerId, "'s triggered ability", spellEntry, null,
                        playerId, NO_EXTRA_RECORD);
            }
        }
    }


    /**
     * "Whenever this creature becomes the target of a spell or ability an opponent controls, &lt;non-counter effect&gt;."
     * Handles the abilities of {@code Triggers.becomesTargetOfOpponentSpellOrAbility()} that don't act on "that spell or
     * ability" (e.g. "you may draw a card"); the counter and ward abilities are
     * {@link #collectBecomesTargetOfOpponentCounterTriggers}'s. Called from both the spell and ability paths so the
     * "or ability" clause is honored. The record names the spell or ability's controller as the player whose
     * permanents a target "that player controls" is judged against. Used by Tenured Concocter, and by the ability
     * Lae'zel, Githyanki Warrior grants the creature it becomes.
     */
    private void collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        UUID spellOrAbilityControllerId = triggeringEntry.getControllerId();
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(spellOrAbilityControllerId)) return;

        Trigger watch = Triggers.becomesTargetOfOpponentSpellOrAbility();
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (!source.isCloaked() && !source.isFaceDown()
                && !gameQueryService.hasLostPrintedAbilities(gameData, source)) {
            abilities.addAll(unjudgedTargetAbilities(printed(source.getCard(), watch), watch));
        }
        // Abilities granted to this permanent itself still apply when its printed ones don't.
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.temporary(source, watch), watch));
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.persistent(source, watch), watch));

        for (TriggeredAbility ability : abilities) {
            if (actsOnASpell(ability)) continue;
            putTargetAbility(gameData, ability, source, controllerId, "'s triggered ability", triggeringEntry, source,
                    controllerId, entry -> {
                        entry.setAttackedTargetId(spellOrAbilityControllerId);
                        entry.setSourcePermanentSnapshot(new Permanent(source));
                    });
        }
    }


    private void collectBecomesTargetOfOpponentSpellOnlyTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (controllerId.equals(spellEntry.getControllerId())) return;

        // "Whenever this creature becomes the target of a spell an opponent controls" (Reality Smasher) sees spells
        // only; a face-down permanent, or one whose printed abilities were removed, has none of its printed abilities.
        for (TriggeredAbility ability : targetAbilities(gameData, source,
                Triggers.becomesTargetOfOpponentSpell(),
                !source.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, source))) {
            putTargetAbility(gameData, ability, source, controllerId, "'s triggered ability", spellEntry, source,
                    controllerId, NO_EXTRA_RECORD);
        }
    }


    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@code Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()}. Only fires when the
     * targeted permanent is a creature and the spell/ability is controlled by an opponent of the creature's
     * controller. The record names the targeted creature as the triggering permanent ("other than that creature",
     * Pawpatch Recruit) and the spell or ability's controller as the triggering player ("that player", Scalelord
     * Reckoner).
     */
    private void collectAllyCreatureBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        // Only trigger for creatures
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        // Only trigger if the spell/ability is controlled by an opponent
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            Trigger watch = Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility();
            List<TriggeredAbility> abilities = new ArrayList<>(printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));

            for (TargetEffect read : targetReads(abilities, watch)) {
                requireWatchedEvent(read, watch);
                requireNoLimit(read, watch);
                // "Whenever a Dragon you control becomes the target ..." (Thunderbreak Regent, Diffusion Sliver).
                if (!targetFilterMatches(gameData, source, creatureControllerId, targetPermanent, read)) continue;
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        });
            }
        }
    }


    /**
     * Checks the spell-only opponent-target trigger slot. This is separate from the
     * spell-or-ability slot because cards such as Swarm Shambler do not trigger from abilities.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        if (creatureControllerId.equals(spellEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            Trigger watch = Triggers.creatureYouControlBecomesTargetOfOpponentSpell();
            List<TriggeredAbility> abilities = new ArrayList<>(printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));

            for (TargetEffect read : targetReads(abilities, watch)) {
                requireWatchedEvent(read, watch);
                requireNoLimit(read, watch);
                // "Whenever a creature you control with a +1/+1 counter on it becomes the target" (Swarm Shambler).
                if (!targetFilterMatches(gameData, source, creatureControllerId, targetPermanent, read)) continue;
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        spellEntry, targetPermanent, creatureControllerId, NO_EXTRA_RECORD);
            }
        }
    }


    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@code Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell()}. Only fires when the
     * targeted permanent is a creature and the targeting spell is an instant or a sorcery; there is
     * no controller restriction, so the creature's controller's own spells trigger it too. The
     * targeted creature is the record's non-targeting {@code targetId} ("that creature"). The trigger's filter is
     * judged on the targeted creature while the trigger event is collected. Used by Wild Defiance and Silverfur
     * Partisan.
     */
    private void collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        if (!spellEntry.getCard().hasType(CardType.INSTANT) && !spellEntry.getCard().hasType(CardType.SORCERY)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            Trigger watch = Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell();
            for (TargetEffect read : targetReads(printed(source.getCard(), watch), watch)) {
                requireWatchedEvent(read, watch);
                requireNoLimit(read, watch);
                // "Whenever a Wolf or Werewolf you control becomes the target" (Silverfur Partisan).
                if (!targetFilterMatches(gameData, source, creatureControllerId, targetPermanent, read)) continue;
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        spellEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                        });
            }
        }
    }


    /**
     * "Whenever a creature you control becomes the target of a spell" (Gargos, Vicious Watcher; Venerated Rotpriest):
     * the targeted creature is the record's non-targeting {@code targetId} ("that creature", "its controller").
     */
    private void collectAllyCreatureBecomesTargetOfSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, source);
            if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            for (TriggeredAbility ability : targetAbilities(gameData, source,
                    Triggers.creatureYouControlBecomesTargetOfSpell(), true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        });
            }
        }
    }


    /**
     * Checks all other permanents on the targeted creature's controller's battlefield for
     * {@code Triggers.anotherCreatureYouControlBecomesTargetOfSpellOrAbility()}. The
     * targeted creature is the record's non-targeting {@code targetId}.
     */
    private void collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.getId().equals(targetPermanent.getId())
                    || source.isCloaked()
                    || source.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            Trigger watch = Triggers.anotherCreatureYouControlBecomesTargetOfSpellOrAbility();
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                        });
            }
        }
    }


    /**
     * Whether the spell or ability that targets is one the ability's {@link TargetTrigger#source()} admits: an ability
     * of a permanent matching it, judged from the watcher's point of view ("an ability of a land you control named
     * Labyrinth of Skophos"). A spell has no such permanent.
     */
    private boolean targetingSourceMatches(GameData gameData, TargetEffect effect, StackEntry triggeringEntry,
                                           FilterContext watcherContext) {
        if (effect.source() == null) {
            return true;
        }
        if (triggeringEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || triggeringEntry.getSourcePermanentId() == null) {
            return false;
        }
        Permanent abilitySource = gameQueryService.findPermanentById(gameData, triggeringEntry.getSourcePermanentId());
        return abilitySource != null
                && predicateEvaluationService.matchesPermanentPredicate(abilitySource, effect.source(), watcherContext);
    }

    /**
     * Checks ALL permanents across every battlefield for
     * {@code Triggers.creatureBecomesTargetOfSpellOrAbility()}. Only fires when the
     * targeted permanent is a creature. The targeted creature is the record's non-targeting
     * {@code targetId} so the resolved effect can act on it. Used by Cowardice.
     */
    private void collectAnyCreatureBecomesTargetTriggers(
            GameData gameData, Permanent targetPermanent, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent source : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
                Trigger watch = Triggers.creatureBecomesTargetOfSpellOrAbility();
                FilterContext watcherContext = FilterContext.of(gameData)
                        .withSourceControllerId(playerId).withSourcePermanentId(source.getId());
                for (TargetEffect read : targetReads(printed(source.getCard(), watch), watch)) {
                    requireWatchedEvent(read, watch);
                    requireNoLimit(read, watch);
                    // "Whenever a Cleric creature you control becomes the target" (Daru Spiritualist).
                    if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                            targetPermanent, read.filter(), watcherContext)
                            || !targetingSourceMatches(gameData, read, triggeringEntry, watcherContext)) {
                        continue;
                    }
                    putTargetAbility(gameData, read.ability(), source, playerId, "'s triggered ability",
                            triggeringEntry, targetPermanent, targetControllerId, entry -> {
                                entry.setTargetId(targetPermanent.getId());
                                entry.setTriggeringPermanentId(targetPermanent.getId());
                            });
                }
            }
        }
    }


    /**
     * Checks the spell/ability controller's battlefield for
     * {@code Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility()}. Only fires
     * when the targeted permanent is a creature controlled by an opponent of the spell/ability's
     * controller. The targeted creature is the record's non-targeting {@code targetId}. Used by Willbreaker.
     */
    private void collectOpponentCreatureBecomesTargetOfYourSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        UUID triggeringControllerId = triggeringEntry.getControllerId();
        if (triggeringControllerId == null || triggeringControllerId.equals(creatureControllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(triggeringControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            Trigger watch = Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility();
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            for (TriggeredAbility ability : unjudgedTargetAbilities(printed(source.getCard(),
                    watch), watch)) {
                putTargetAbility(gameData, ability, source, triggeringControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                        });
            }
        }
    }


    /**
     * Whether the filter of {@code effect}'s becomes-the-target ability ({@link TargetEffect#filter()}) admits
     * {@code targetPermanent}, judged from {@code watcher}'s point of view: always without a filter, never for a
     * player target (no permanent) with one.
     */
    private boolean targetFilterMatches(GameData gameData, Permanent watcher, UUID watcherControllerId,
                                        Permanent targetPermanent, TargetEffect effect) {
        if (effect.filter() == null) {
            return true;
        }
        return targetPermanent != null && predicateEvaluationService.matchesPermanentPredicate(targetPermanent,
                effect.filter(), watcherFilterContext(gameData, watcher, watcherControllerId));
    }

    /** The becomes-the-target abilities other permanents' static abilities grant {@code source} that {@code watch} reads. */
    private List<TriggeredAbility> grantedTargetAbilities(GameData gameData, Permanent source, Trigger watch) {
        return grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, source, watch);
    }

    /**
     * Whether a static ability of another permanent grants {@code source} a "becomes the target of a spell or ability
     * an opponent controls" ability that acts on "that spell or ability" (a granted ward, CR 702.21a).
     */
    private boolean hasGrantedCounterUnlessTrigger(GameData gameData, Permanent source) {
        Trigger watch = Triggers.becomesTargetOfOpponentSpellOrAbility();
        return unjudgedTargetAbilities(grantedTargetAbilities(gameData, source, watch), watch).stream()
                .anyMatch(TargetTriggers::actsOnASpell);
    }

    private void collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)
                || triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || triggeringEntry.getCard() == null
                || !triggeringEntry.getCard().getKeywords().contains(Keyword.BACKUP)
                || !isBackupAbility(triggeringEntry)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            Trigger watch = Triggers.creatureYouControlBecomesTargetOfBackupAbility();
            List<TriggeredAbility> abilities = new ArrayList<>(printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));
            // "This ability triggers only once each turn" (Mirror-Shield Hoplite) is that ability's limit, judged and
            // marked by the door. "Copy that ability" is the backup ability, the record's non-targeting target.
            for (TargetEffect read : targetReads(abilities, watch)) {
                requireWatchedEvent(read, watch);
                requireNoFilter(read, watch);
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId,
                        entry -> entry.setTargetId(triggeringEntry.getTargetableId()));
            }
        }
    }


    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            Trigger watch = Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility();
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, NO_EXTRA_RECORD);
            }
        }
    }


    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, StackEntry targetedEntry, StackEntry triggeringEntry) {
        if (targetedEntry == null || targetedEntry.getEntryType() != StackEntryType.CREATURE_SPELL) return;

        UUID creatureControllerId = targetedEntry.getControllerId();
        if (creatureControllerId == null || creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            Trigger watch = Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility();
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, null, creatureControllerId, NO_EXTRA_RECORD);
            }
        }
    }


    private boolean hasWardAbility(GameData gameData, Permanent source) {
        if (source.isFaceDown()
                && (source.isCloaked() || source.isFaceDownWard())
                && gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            return true;
        }
        if (gameQueryService.isCreature(gameData, source)
                && (gameQueryService.hasKeyword(gameData, source, Keyword.WARD)
                || hasGrantedCounterUnlessTrigger(gameData, source))) {
            return true;
        }
        if (source.isAttached() && source.getAttachedTo() != null) {
            Permanent attachedTo = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
            return attachedTo != null && (attachedTo.isCloaked()
                    || (gameQueryService.isCreature(gameData, attachedTo)
                    && gameQueryService.hasKeyword(gameData, attachedTo, Keyword.WARD)));
        }
        return false;
    }


    /** The filter context of a predicate {@code watcher}, a permanent with a triggered ability, judges. */
    private static FilterContext watcherFilterContext(GameData gameData, Permanent watcher, UUID watcherControllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher);
    }

    /**
     * The watched trigger of the one collector that reads "one or more" ({@link TargetEffect#oneOrMore()}): "you and/or
     * at least one permanent you control becomes the target".
     */
    private static final Trigger ONE_OR_MORE_READER =
            Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility();

    /** The watched trigger of the one collector that reads a {@link TargetTrigger#source()}. */
    private static final Trigger SOURCE_READER =
            Triggers.creatureBecomesTargetOfSpellOrAbility();


    /**
     * One effect of a becomes-the-target ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the permanent that becomes the target must also be for the watched condition (its
     *                {@link TargetTrigger#qualifier()}), or {@code null}
     * @param event   the watched condition's own event, which may be narrower than the watch's
     *                ({@link TargetTrigger#watchedEvent()})
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     * @param source  what the permanent whose ability does the targeting must be (its {@link TargetTrigger#source()}),
     *                or {@code null}; only the "a creature becomes the target of a spell or ability" collector reads it
     */
    record TargetEffect(CardEffect effect, TriggeredAbility ability,
                               PermanentPredicate filter, TargetTrigger.TargetEvent event, boolean simultaneousWithPrevious,
                               PermanentPredicate source) {

        public TargetEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(event, "event");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /**
         * "Whenever you and/or at least one permanent you control becomes the target of a spell or ability" (Leyline of
         * Combustion): once for the spell or ability however many of them it targets, where the default triggers once
         * for each such target (CR 603.2c).
         */
        public boolean oneOrMore() {
            return ability.options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        }

        /**
         * Whether only a spell or ability the ability's controller controls triggers it, read by the collector of the
         * event about any controller: "this becomes the target of a spell you control", "of a spell or ability you
         * control", "a player or permanent becomes the target of an ability you control".
         */
        public boolean byController() {
            return event == TargetTrigger.TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL
                    || event == TargetTrigger.TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY
                    || event == TargetTrigger.TargetEvent.PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY;
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now: "triggers only once each
         * turn" ("for the first time each turn" included) and "this ability triggers only once", as
         * {@link TriggeredAbility#limitKey()} marked them.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    private static List<TargetEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<TargetEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities}, one list for each ability that has any, in order, as their collector reads them
     * for the condition watched as {@code watched}: each list is one object on the stack (CR 603.3), whose
     * trigger-level parts (filter, event, source, limit) every read of it shares. Two equal abilities, or one ability
     * a permanent has twice (its own ward and a granted one), are two lists.
     */
    private static List<List<TargetEffect>> readByAbility(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<TargetEffect>> byAbility = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<TargetEffect> effects = read(ability, watched);
            if (!effects.isEmpty()) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The
     * becomes-the-target collectors trigger once for each targeting spell or ability and each permanent it targets, or
     * once for each ability for "one or more creatures you control become the target of an activated ability", by the
     * event; of the multiplicities only the "you or a permanent you control becomes the target" collector reads one
     * ("one or more": "you and/or at least one permanent you control"), and of the limits they read "triggers only once
     * each turn" and "this ability triggers only once".
     */
    private static List<TargetEffect> read(TriggeredAbility ability, Trigger watched) {
        TargetTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH
                && !(options.multiplicity() == TriggerMultiplicity.ONE_OR_MORE && watched.equals(ONE_OR_MORE_READER))) {
            throw new IllegalStateException("The becomes-the-target collector of " + watched + " reads no "
                    + options.multiplicity() + ": " + ability);
        }
        if (options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN
                || options.limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No becomes-the-target collector reads " + options.limit() + ": "
                    + ability);
        }
        if (condition.source() != null && !watched.equals(SOURCE_READER)) {
            throw new IllegalStateException("Only the collector of " + SOURCE_READER + " reads a source: " + ability);
        }
        Condition interveningIf = options.interveningIf();
        List<TargetEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new TargetEffect(effect, ability, condition.qualifier(),
                    condition.event(), registration.simultaneousWithPrevious(), condition.source()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that reads no
     * filter, narrower event or limit: none of its abilities is written with one, and this throws on one.
     */
    private static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<TargetEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effectsOf(effects);
    }

    /** The effects of {@code effects}, in order. */
    private static List<CardEffect> effectsOf(List<TargetEffect> effects) {
        return effects.stream().map(TargetEffect::effect).toList();
    }

    /**
     * Throws when {@code effect}'s ability has a filter, a narrower event or a limit, which the collector of
     * {@code watched} doesn't read.
     */
    private static void requireNothingToJudge(TargetEffect effect, Trigger watched) {
        requireNoFilter(effect, watched);
        requireWatchedEvent(effect, watched);
        requireNoLimit(effect, watched);
    }

    /** Throws when {@code effect}'s ability has a filter, which the collector of {@code watched} doesn't read. */
    private static void requireNoFilter(TargetEffect effect, Trigger watched) {
        if (effect.filter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability has an event narrower than {@code watched}'s ("a spell you control"),
     * which the collector of {@code watched} doesn't read.
     */
    private static void requireWatchedEvent(TargetEffect effect, Trigger watched) {
        if (effect.event() != ((TargetTrigger) watched).event()) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.event() + ": "
                    + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a limit, which the collector of {@code watched} doesn't read. */
    private static void requireNoLimit(TargetEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    /**
     * {@code ability}'s target condition watched as {@code watched}: the one whose {@link Trigger#watchKey()} it is.
     * Two conditions of one ability the same watch reads with a different filter or event ("a spell you control" and
     * "a spell") would be read as one, so they are rejected.
     */
    private static TargetTrigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<TargetTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(TargetTrigger.class::isInstance).map(TargetTrigger.class::cast)
                .filter(target -> target.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no becomes-the-target condition watched as " + watched);
        }
        if (conditions.size() > 1) {
            throw new IllegalStateException("The becomes-the-target conditions of one ability watched as " + watched
                    + " must read alike: " + ability);
        }
        return conditions.getFirst();
    }

    /**
     * Whether {@code entry} is a backup ability: the "when this creature enters, put N +1/+1 counters on target
     * creature" ability of its card (CR 702.165a), not another triggered ability of a creature with backup.
     */
    private static boolean isBackupAbility(StackEntry entry) {
        return entry.getEffectsToResolve().stream().anyMatch(entry.getCard().getOwnEntryEffects()::contains);
    }
}
