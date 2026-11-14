package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.BlockEvent;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Triggers the block abilities of a {@link BlockEvent}, each handed whole to the stack door with the record of the
 * block, its candidates read from {@link TriggerIndex} as the layers leave them and matched by their combat trigger
 * condition ({@link TriggerMatcher#combatCondition}). For a declaration of blockers, in this order: each blocker's
 * "whenever this creature blocks" and its Auras' and Equipment's; "blocks two or more creatures"; "whenever a creature
 * blocks"; for each blocked attacker its "becomes blocked" abilities, its Auras' and Equipment's, and "whenever a
 * creature you control becomes blocked"; "one or more creatures you control become blocked"; "whenever a creature
 * becomes blocked"; "whenever one or more creatures block"; then the unblocked attackers' "attacks and isn't blocked"
 * abilities. A block an effect makes, a creature entering the battlefield blocking and an attacker an effect makes
 * blocked trigger the subset of these the event is (CR 509.1h, 509.3). The block's caller orders the batch APNAP
 * (CR 603.3b).
 *
 * <p>"Blocks" once each combat ({@code ONE_OR_MORE}) triggers for a blocker's first block only (CR 509.3a); "becomes
 * blocked by a creature" ({@code EACH_BLOCKER}) once for each blocker (CR 509.3d), its filter judged on the blocker
 * (CR 509.3f). An ability's event condition (CR 603.2) is judged here; the door judges its intervening "if" (CR 603.4).
 */
@Service
public class BlockTriggers {

    private final TriggerIndex triggerIndex;
    private final TriggeredAbilityStackService stackService;
    private final AttackTriggers attackTriggers;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;

    public BlockTriggers(TriggerIndex triggerIndex, @Lazy TriggeredAbilityStackService stackService,
                         @Lazy AttackTriggers attackTriggers, @Lazy GameQueryService gameQueryService,
                         @Lazy PredicateEvaluationService predicateEvaluationService,
                         @Lazy ConditionEvaluationService conditionEvaluationService,
                         @Lazy GameLogService gameLogService) {
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.attackTriggers = attackTriggers;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
    }

    /** A block of the event, its creatures as they are now. */
    private record Pair(Permanent blocker, Permanent attacker) {
    }

    /** Triggers the block abilities {@code event} triggers, as the class describes. */
    public void trigger(GameData gameData, BlockEvent event) {
        UUID activeId = event.attackingPlayerId();
        UUID defenderId = event.defendingPlayerId();
        List<Pair> pairs = new ArrayList<>();
        for (BlockEvent.Block block : event.blocks()) {
            Permanent blocker = gameQueryService.findPermanentById(gameData, block.blockerId());
            Permanent attacker = gameQueryService.findPermanentById(gameData, block.attackerId());
            if (blocker != null && attacker != null) {
                pairs.add(new Pair(blocker, attacker));
            }
        }
        List<Permanent> blocked = permanents(gameData, event.blockedAttackerIds());
        Set<String> oncePerBlock = new HashSet<>();
        switch (event.kind()) {
            case DECLARED -> {
                Set<UUID> blockedOnce = new HashSet<>();
                for (Pair pair : pairs) {
                    List<Permanent> blockedByBlocker = pairs.stream()
                            .filter(other -> other.blocker() == pair.blocker()).map(Pair::attacker).toList();
                    ownBlock(gameData, pair.blocker(), pair.attacker(), defenderId, blockedByBlocker,
                            blockedOnce.add(pair.blocker().getId()));
                    attachedCombat(gameData, pair.blocker(), Triggers.blocks(), pair.attacker(), oncePerBlock);
                }
                Map<Permanent, Integer> blocksPerBlocker = new LinkedHashMap<>();
                pairs.forEach(pair -> blocksPerBlocker.merge(pair.blocker(), 1, Integer::sum));
                blocksPerBlocker.forEach((blocker, count) -> {
                    if (count >= 2) {
                        blocksTwoOrMore(gameData, blocker, defenderId);
                    }
                });
                creatureBlocks(gameData, pairs);
                for (Permanent attacker : blocked) {
                    List<Permanent> blockers = pairs.stream().filter(pair -> pair.attacker() == attacker)
                            .map(Pair::blocker).toList();
                    becomesBlocked(gameData, attacker, activeId, blockers, true);
                    attachedCombat(gameData, attacker, Triggers.becomesBlocked(), null, oncePerBlock);
                    attachedPerBlocker(gameData, attacker, blockers);
                    creatureYouControlBecomesBlocked(gameData, activeId, attacker);
                }
                creaturesYouControlBecomeBlocked(gameData, activeId, blocked);
                creatureBecomesBlocked(gameData, blocked, pairs);
                creaturesBlock(gameData, pairs);
                unblocked(gameData, activeId, defenderId, permanents(gameData, event.unblockedAttackerIds()));
            }
            case NONE_DECLARED -> unblocked(gameData, activeId, defenderId,
                    permanents(gameData, event.unblockedAttackerIds()));
            case EFFECT -> {
                if (pairs.isEmpty()) {
                    return;
                }
                Pair pair = pairs.getFirst();
                ownBlock(gameData, pair.blocker(), pair.attacker(), defenderId, List.of(pair.attacker()),
                        !event.blockerWasBlocking());
                attachedCombat(gameData, pair.blocker(), Triggers.blocks(), pair.attacker(), oncePerBlock);
                if (pair.blocker().getBlockingTargetIds().size() >= 2) {
                    blocksTwoOrMore(gameData, pair.blocker(), defenderId);
                }
                creatureBlocks(gameData, pairs);
                if (!blocked.isEmpty()) {
                    becomesBlocked(gameData, pair.attacker(), activeId, List.of(pair.blocker()), true);
                    attachedCombat(gameData, pair.attacker(), Triggers.becomesBlocked(), null, oncePerBlock);
                    attachedPerBlocker(gameData, pair.attacker(), List.of(pair.blocker()));
                    creatureYouControlBecomesBlocked(gameData, activeId, pair.attacker());
                    creatureBecomesBlocked(gameData, List.of(pair.attacker()), pairs);
                }
                creaturesBlock(gameData, pairs);
            }
            case ENTERED_BLOCKING -> {
                if (pairs.isEmpty()) {
                    return;
                }
                Pair pair = pairs.getFirst();
                becomesBlocked(gameData, pair.attacker(), activeId, List.of(pair.blocker()), event.newlyBlocked());
                if (event.newlyBlocked()) {
                    attachedCombat(gameData, pair.attacker(), Triggers.becomesBlocked(), null, null);
                    creatureYouControlBecomesBlocked(gameData, activeId, pair.attacker());
                }
            }
            case WITHOUT_BLOCKERS -> {
                for (Permanent attacker : blocked) {
                    becomesBlocked(gameData, attacker, activeId, List.of(), true);
                    attachedCombat(gameData, attacker, Triggers.becomesBlocked(), null, null);
                    creatureYouControlBecomesBlocked(gameData, activeId, attacker);
                    creatureBecomesBlocked(gameData, attacker);
                }
            }
        }
    }

    private List<Permanent> permanents(GameData gameData, List<UUID> ids) {
        List<Permanent> permanents = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                permanents.add(permanent);
            }
        }
        return permanents;
    }

    private List<TriggeredAbility> abilities(GameData gameData, Permanent permanent, CombatTrigger watched) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.permanentAbilities(gameData, permanent)) {
            if (TriggerMatcher.combatCondition(ability, watched, Zone.BATTLEFIELD) != null) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    private static PermanentPredicate filter(TriggeredAbility ability, CombatTrigger watched) {
        CombatTrigger condition = TriggerMatcher.combatCondition(ability, watched, null);
        return condition == null ? null : condition.filter();
    }

    private static void requireEach(TriggeredAbility ability, CombatTrigger watched) {
        if (ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No " + watched + " ability is written with "
                    + ability.options().multiplicity() + ": " + ability);
        }
    }

    private static void requireNoFilterOrMultiplicity(TriggeredAbility ability, CombatTrigger watched) {
        requireEach(ability, watched);
        if (filter(ability, watched) != null) {
            throw new IllegalStateException("No " + watched + " ability is written with a filter: " + ability);
        }
    }

    private static void requirePlain(TriggeredAbility ability, CombatTrigger watched) {
        requireNoFilterOrMultiplicity(ability, watched);
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("No " + watched + " ability is written with a limit: " + ability);
        }
    }

    private boolean eventConditionMet(GameData gameData, TriggeredAbility ability, ConditionContext context) {
        return conditionEvaluationService.eventConditionMet(gameData, ability.options(), context);
    }

    private static FilterContext watcherContext(GameData gameData, Permanent watcher, UUID controllerId) {
        return FilterContext.of(gameData).withSourceCardId(watcher.getCard().getId())
                .withSourceControllerId(controllerId).withSourcePermanentId(watcher.getId());
    }

    private void put(GameData gameData, TriggeredAbilityRequest request, Card logCard, String logText) {
        if (stackService.put(gameData, request)) {
            gameLogService.append(gameData, GameLog.cardThen(logCard, logText));
        }
    }

    /**
     * Records "that creature", the creature on the other side of a block, whatever the effects are: its triggering
     * permanent with its controller and as it is now (its last-known information once it leaves, CR 608.2h), its
     * non-targeting target (CR 115.10a), and its power and toughness as the ability triggered.
     */
    private void recordCombatOpponent(GameData gameData, StackEntry entry, Permanent opponent) {
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(opponent.getId());
        entry.setTriggeringPermanentControllerId(gameQueryService.findPermanentController(gameData, opponent.getId()));
        entry.setTriggeringPermanent(opponent);
        entry.setCombatOpponentPowerAtTrigger(gameQueryService.getEffectivePower(gameData, opponent));
        entry.setCombatOpponentToughnessAtTrigger(gameQueryService.getEffectiveToughness(gameData, opponent));
    }

    /**
     * Records the creature that became blocked: its triggering permanent with its controller and as it is now, its
     * non-targeting target (CR 115.10a) and the player or planeswalker it's attacking.
     */
    private void recordBlockedCreature(GameData gameData, StackEntry entry, Permanent blocked) {
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(blocked.getId());
        entry.setTriggeringPermanentControllerId(gameQueryService.findPermanentController(gameData, blocked.getId()));
        entry.setTriggeringPermanent(blocked);
        entry.setAttackedTargetId(blocked.getAttackTarget());
    }

    /**
     * "Whenever this creature blocks" of {@code blocker} for its block of {@code attacker}: "blocks" once each combat
     * only for its first block (CR 509.3a; bushido, CR 702.45a), "blocks a creature" for each (CR 509.3b). "A [filter]
     * creature" is judged on the attacker, "one or more [filter] creatures" on any it blocks (CR 509.3f). "That
     * creature" is the attacker.
     */
    private void ownBlock(GameData gameData, Permanent blocker, Permanent attacker, UUID defenderId,
                          List<Permanent> blockedByBlocker, boolean firstBlock) {
        CombatTrigger watched = Triggers.blocks();
        ConditionContext context = ConditionContext.forPermanent(blocker, defenderId)
                .withTriggeringPermanentId(attacker.getId());
        for (TriggeredAbility ability : abilities(gameData, blocker, watched)) {
            TriggerMultiplicity multiplicity = ability.options().multiplicity();
            if (multiplicity == TriggerMultiplicity.EACH_BLOCKER) {
                throw new IllegalStateException("No block ability is written with " + ability.options() + ": "
                        + ability);
            }
            boolean oneOrMore = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
            if (oneOrMore && !firstBlock) {
                continue;
            }
            PermanentPredicate filter = filter(ability, watched);
            if (filter != null && (oneOrMore
                    ? blockedByBlocker.stream().noneMatch(other ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, other, filter))
                    : !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, filter))) {
                continue;
            }
            if (!eventConditionMet(gameData, ability, context)) {
                continue;
            }
            put(gameData, new TriggeredAbilityRequest(ability, blocker.getCard(), blocker, defenderId,
                    blocker.getCard().getName() + "'s block trigger", context, entry -> {
                        entry.setTargetId(attacker.getId());
                        recordCombatOpponent(gameData, entry, attacker);
                        entry.setSourcePermanentSnapshot(new Permanent(blocker));
                    }), blocker.getCard(), "'s block ability triggers.");
        }
    }

    /**
     * "Whenever enchanted (equipped) creature blocks / becomes blocked" of the Auras and Equipment attached to
     * {@code creature}: the filter on the creature on the other side of the block, {@code combatOpponent} or, without
     * one, any creature blocking it ("becomes blocked by one or more colorless creatures", Hedron Blade). An Aura's
     * ability granted to the creature is lost with the creature's abilities (Turn to Frog). {@code oncePerBlock} holds
     * the "blocks" once each combat abilities that triggered in this declaration (CR 509.3a).
     */
    private void attachedCombat(GameData gameData, Permanent creature, CombatTrigger watched,
                                Permanent combatOpponent, Set<String> oncePerBlock) {
        UUID creatureControllerId = gameData.findControllerOf(creature);
        if (creatureControllerId == null) {
            return;
        }
        boolean creatureLostAllAbilities = gameQueryService.hasLostAllAbilities(gameData, creature);
        ConditionContext context = ConditionContext.forPermanent(creature, creatureControllerId);
        gameData.forEachPermanent((attachedControllerId, attached) -> {
            if (!attached.isAttached() || !attached.getAttachedTo().equals(creature.getId())) {
                return;
            }
            if (creatureLostAllAbilities && attached.getCard().isAura()
                    && !attached.getCard().isAttackTriggerOwnedByAura()) {
                return;
            }
            for (TriggeredAbility ability : abilities(gameData, attached, watched)) {
                if (ability.options().limit() != TriggerLimit.NONE) {
                    throw new IllegalStateException("No attached permanent's block ability is written with a limit: "
                            + ability);
                }
                TriggerMultiplicity multiplicity = ability.options().multiplicity();
                if (multiplicity == TriggerMultiplicity.EACH_BLOCKER) {
                    continue;
                }
                boolean oneOrMore = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
                String key = attached.getId() + ":" + ability.limitKey();
                if (oneOrMore && oncePerBlock != null && oncePerBlock.contains(key)) {
                    continue;
                }
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                PermanentPredicate blockFilter = filter(ability, watched);
                if (blockFilter != null && !matchesCombatOpponent(gameData, creature, combatOpponent, blockFilter)) {
                    continue;
                }
                if (stackService.put(gameData, new TriggeredAbilityRequest(ability, attached.getCard(), attached,
                        attachedControllerId, attached.getCard().getName() + "'s triggered ability", context,
                        entry -> attackTriggers.recordAttachedEvent(gameData, entry, creature, creatureControllerId,
                                combatOpponent, attached)))) {
                    if (oneOrMore && oncePerBlock != null) {
                        oncePerBlock.add(key);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(attached.getCard()));
                }
            }
        });
    }

    private boolean matchesCombatOpponent(GameData gameData, Permanent creature, Permanent combatOpponent,
                                          PermanentPredicate filter) {
        if (combatOpponent != null) {
            return predicateEvaluationService.matchesPermanentPredicate(gameData, combatOpponent, filter);
        }
        for (UUID blockedId : creature.getBlockingTargetIds()) {
            Permanent blocked = gameQueryService.findPermanentById(gameData, blockedId);
            if (blocked != null && predicateEvaluationService.matchesPermanentPredicate(gameData, blocked, filter)) {
                return true;
            }
        }
        boolean[] matches = {false};
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (!matches[0] && permanent.isBlocking() && permanent.getBlockingTargetIds().contains(creature.getId())
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                matches[0] = true;
            }
        });
        return matches[0];
    }

    /**
     * "Whenever equipped creature becomes blocked by a creature" (Infiltration Lens), once for each blocker: the filter
     * on the blocker, the event condition read from {@code attacker}. "That creature" is the blocker.
     */
    private void attachedPerBlocker(GameData gameData, Permanent attacker, List<Permanent> blockers) {
        UUID controllerId = gameData.findControllerOf(attacker);
        if (controllerId == null) {
            return;
        }
        CombatTrigger watched = Triggers.becomesBlocked();
        ConditionContext context = ConditionContext.forPermanent(attacker, controllerId);
        gameData.forEachPermanent((attachedControllerId, attached) -> {
            if (!attached.isAttached() || !attached.getAttachedTo().equals(attacker.getId())) {
                return;
            }
            List<TriggeredAbility> perBlocker = abilities(gameData, attached, watched).stream()
                    .filter(ability -> ability.options().multiplicity() == TriggerMultiplicity.EACH_BLOCKER)
                    .toList();
            for (Permanent blocker : blockers) {
                for (TriggeredAbility ability : perBlocker) {
                    PermanentPredicate filter = filter(ability, watched);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker,
                            filter)) || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    if (stackService.put(gameData, new TriggeredAbilityRequest(ability, attached.getCard(), attached,
                            attachedControllerId, attached.getCard().getName() + "'s triggered ability", context,
                            entry -> {
                                entry.setTargetId(blocker.getId());
                                entry.setNonTargeting(true);
                                entry.setTriggeringPermanentId(attacker.getId());
                                entry.setTriggeringPermanentControllerId(controllerId);
                                entry.setAttachedPermanentSnapshot(new Permanent(attacker));
                            }))) {
                        gameLogService.append(gameData, GameLog.abilityTriggers(attached.getCard()));
                    }
                }
            }
        });
    }

    /** "Whenever this creature blocks two or more creatures" of {@code blocker} (CR 509.3e). */
    private void blocksTwoOrMore(GameData gameData, Permanent blocker, UUID defenderId) {
        CombatTrigger watched = Triggers.blocksTwoOrMore();
        ConditionContext context = ConditionContext.forPermanent(blocker, defenderId);
        for (TriggeredAbility ability : abilities(gameData, blocker, watched)) {
            requireNoFilterOrMultiplicity(ability, watched);
            if (!eventConditionMet(gameData, ability, context)) {
                continue;
            }
            put(gameData, new TriggeredAbilityRequest(ability, blocker.getCard(), blocker, defenderId,
                    blocker.getCard().getName() + "'s multi-block trigger", context, entry -> {
                        entry.setTargetId(blocker.getId());
                        entry.setNonTargeting(true);
                    }), blocker.getCard(), "'s block ability triggers.");
        }
    }

    /**
     * "Whenever a creature blocks" of every permanent, once for each blocking creature (CR 509.3a): the filter on the
     * blocker from the watcher's point of view, the blocker "that creature"; then the delayed ones watching every
     * blocker ("a creature an opponent controls blocks", Benefactor's Draught).
     */
    private void creatureBlocks(GameData gameData, List<Pair> pairs) {
        CombatTrigger watched = Triggers.creatureBlocks();
        Set<Permanent> blockers = new java.util.LinkedHashSet<>();
        pairs.forEach(pair -> blockers.add(pair.blocker()));
        for (Permanent blocker : blockers) {
            for (UUID controllerId : gameData.orderedPlayerIds) {
                for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId,
                        List.of()))) {
                    ConditionContext context = ConditionContext.forPermanent(watcher, controllerId)
                            .withTriggeringPermanentId(blocker.getId());
                    for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                        requireEach(ability, watched);
                        PermanentPredicate filter = filter(ability, watched);
                        if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(blocker, filter,
                                FilterContext.of(gameData).withSourceCardId(watcher.getCard().getId())
                                        .withSourceControllerId(controllerId)))
                                || !eventConditionMet(gameData, ability, context)) {
                            continue;
                        }
                        put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, controllerId,
                                watcher.getCard().getName() + "'s block trigger", context, entry -> {
                                    entry.setTargetId(blocker.getId());
                                    entry.setNonTargeting(true);
                                    entry.getRemovedPermanentControllers().put(blocker.getId(),
                                            gameQueryService.findPermanentController(gameData, blocker.getId()));
                                }), watcher.getCard(), "'s ability triggers.");
                    }
                }
            }
            delayedCreatureBlocks(gameData, blocker);
        }
    }

    /** The delayed "whenever a creature blocks this turn" abilities watching every blocker, for {@code blocker}. */
    private void delayedCreatureBlocks(GameData gameData, Permanent blocker) {
        CombatTrigger watched = Triggers.creatureBlocks();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (TriggerMatcher.combatCondition(watcher.ability(), watched, null) == null
                    || watcher.watchedPermanentId() != null) {
                continue;
            }
            if (watcher.ability().options().limit() != TriggerLimit.NONE
                    || watcher.ability().options().multiplicity() != TriggerMultiplicity.EACH) {
                throw new IllegalStateException("No delayed " + watched + " ability is written with "
                        + watcher.ability().options() + ": " + watcher.ability());
            }
            PermanentPredicate filter = filter(watcher.ability(), watched);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(blocker, filter,
                    FilterContext.of(gameData).withSourceCardId(watcher.sourceCard().getId())
                            .withSourceControllerId(watcher.controllerId()))) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", delayedEffects(watcher));
            entry.setTargetId(blocker.getId());
            entry.setEventValue(0);
            entry.setLinkedExiledCardId(watcher.linkedExiledCardId());
            entry.setNonTargeting(true);
            stackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    private static List<CardEffect> delayedEffects(TemporaryGlobalTriggeredAbility watcher) {
        Condition interveningIf = watcher.ability().options().interveningIf();
        return watcher.ability().effects().stream()
                .map(registration -> interveningIf == null ? registration.effect()
                        : (CardEffect) new ConditionalEffect(interveningIf, registration.effect()))
                .toList();
    }

    /**
     * {@code attacker}'s own "whenever this creature becomes blocked": the delayed ones watching it first (Okoye-like
     * "this combat"), then, once (CR 509.3c) when it is {@code newlyBlocked}, the ones that trigger once, a filter on
     * the blockers ("becomes blocked by one or more Walls"; with no blockers, CR 509.1h, only those with none), then
     * "becomes blocked by a creature" ({@code EACH_BLOCKER}) once for each of {@code blockers} (CR 509.3d) with that
     * blocker as "that creature". The event condition is read from the attacker.
     */
    private void becomesBlocked(GameData gameData, Permanent attacker, UUID activeId, List<Permanent> blockers,
                                boolean newlyBlocked) {
        CombatTrigger watched = Triggers.becomesBlocked();
        if (newlyBlocked) {
            for (TemporaryGlobalTriggeredAbility delayed : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
                if (TriggerMatcher.combatCondition(delayed.ability(), watched, null) == null
                        || !attacker.getId().equals(delayed.watchedPermanentId())) {
                    continue;
                }
                if (filter(delayed.ability(), watched) != null
                        || delayed.ability().options().limit() != TriggerLimit.NONE
                        || delayed.ability().options().multiplicity() != TriggerMultiplicity.EACH) {
                    throw new IllegalStateException("No delayed becomes-blocked ability is written with a filter, "
                            + "limit or multiplicity: " + delayed.ability());
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                        delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed becomes-blocked trigger",
                        delayedEffects(delayed), attacker.getId(), (UUID) null);
                entry.setNonTargeting(true);
                stackService.putDelayed(gameData, entry);
            }
        }
        List<TriggeredAbility> abilities = abilities(gameData, attacker, watched);
        ConditionContext attackerContext = ConditionContext.forPermanent(attacker, activeId);
        if (newlyBlocked) {
            for (TriggeredAbility ability : abilities) {
                PermanentPredicate filter = filter(ability, watched);
                boolean blockersMatch = filter == null || blockers.stream().anyMatch(blocker ->
                        predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, filter));
                if (ability.options().multiplicity() == TriggerMultiplicity.EACH_BLOCKER || !blockersMatch
                        || !eventConditionMet(gameData, ability, attackerContext)) {
                    continue;
                }
                UUID attackedTarget = attacker.getAttackTarget();
                put(gameData, new TriggeredAbilityRequest(ability, attacker.getCard(), attacker, activeId,
                        attacker.getCard().getName() + "'s becomes-blocked trigger", attackerContext, entry -> {
                            entry.setTargetId(attacker.getId());
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(new Permanent(attacker));
                            entry.setAttackedTargetId(attackedTarget);
                            if (attackedTarget != null) {
                                entry.setDefendingPlayerId(gameQueryService.defendingPlayerOfAttackTarget(gameData,
                                        attackedTarget));
                            }
                        }), attacker.getCard(), "'s becomes-blocked ability triggers.");
            }
        }
        for (Permanent blocker : blockers) {
            ConditionContext blockerContext = attackerContext.withTriggeringPermanentId(blocker.getId());
            for (TriggeredAbility ability : abilities) {
                PermanentPredicate filter = filter(ability, watched);
                if (ability.options().multiplicity() != TriggerMultiplicity.EACH_BLOCKER
                        || (filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker,
                        filter)) || !eventConditionMet(gameData, ability, blockerContext)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, attacker.getCard(), attacker, activeId,
                        attacker.getCard().getName() + "'s becomes-blocked trigger", blockerContext, entry -> {
                            entry.setTargetId(blocker.getId());
                            recordCombatOpponent(gameData, entry, blocker);
                        }), attacker.getCard(), "'s becomes-blocked ability triggers.");
            }
        }
    }

    /**
     * "Whenever a creature you control becomes blocked" of the attacking player's permanents, for {@code blocked}: the
     * filter on it from the watcher's point of view, the event condition read from it. "It" is the blocked creature.
     */
    private void creatureYouControlBecomesBlocked(GameData gameData, UUID activeId, Permanent blocked) {
        CombatTrigger watched = Triggers.creatureYouControlBecomesBlocked();
        ConditionContext context = ConditionContext.forPermanent(blocked, activeId)
                .withTriggeringPermanentId(blocked.getId());
        for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(activeId, List.of()))) {
            for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                requireEach(ability, watched);
                PermanentPredicate filter = filter(ability, watched);
                if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(blocked, filter,
                        watcherContext(gameData, watcher, activeId)))
                        || !eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, activeId,
                        watcher.getCard().getName() + "'s becomes-blocked trigger", context, entry -> {
                            entry.setTargetId(blocked.getId());
                            recordBlockedCreature(gameData, entry, blocked);
                            entry.setSourcePermanentSnapshot(new Permanent(watcher));
                        }), watcher.getCard(), "'s ability triggers.");
            }
        }
    }

    /** "Whenever one or more creatures you control become blocked", once for the declaration. */
    private void creaturesYouControlBecomeBlocked(GameData gameData, UUID activeId, List<Permanent> blocked) {
        if (blocked.isEmpty()) {
            return;
        }
        CombatTrigger watched = Triggers.creaturesYouControlBecomeBlocked();
        for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(activeId, List.of()))) {
            ConditionContext context = ConditionContext.forPermanent(watcher, activeId);
            for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                requireNoFilterOrMultiplicity(ability, watched);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, activeId,
                        watcher.getCard().getName() + "'s creatures-become-blocked trigger", context,
                        TriggeredAbilityRequest.TriggerRecord.NONE), watcher.getCard(), "'s ability triggers.");
            }
        }
    }

    /**
     * "Whenever a creature becomes blocked" of every permanent: once for each blocked attacker (CR 509.3c), then
     * "becomes blocked by a creature" ({@code EACH_BLOCKER}, CR 509.3d) once for each block, its filter judged on the
     * attacker and its event condition on the pair (No Quarter's lesser power); the blocker is "the blocking creature".
     */
    private void creatureBecomesBlocked(GameData gameData, List<Permanent> blocked, List<Pair> pairs) {
        for (Permanent attacker : blocked) {
            creatureBecomesBlocked(gameData, attacker);
        }
        CombatTrigger watched = Triggers.creatureBecomesBlocked();
        for (Pair pair : pairs) {
            for (UUID controllerId : gameData.orderedPlayerIds) {
                for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId,
                        List.of()))) {
                    ConditionContext context = ConditionContext.forPermanent(watcher, controllerId)
                            .withTriggeringPermanentId(pair.attacker().getId()).withTargetId(pair.blocker().getId());
                    for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                        if (ability.options().multiplicity() != TriggerMultiplicity.EACH_BLOCKER) {
                            continue;
                        }
                        PermanentPredicate filter = filter(ability, watched);
                        if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(pair.attacker(),
                                filter, watcherContext(gameData, watcher, controllerId)))
                                || !eventConditionMet(gameData, ability, context)) {
                            continue;
                        }
                        put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, controllerId,
                                watcher.getCard().getName() + "'s block trigger", context, entry -> {
                                    entry.setTargetId(pair.blocker().getId());
                                    recordBlockedCreature(gameData, entry, pair.attacker());
                                }), watcher.getCard(), "'s ability triggers.");
                    }
                }
            }
        }
    }

    /**
     * "Whenever a [filter] creature becomes blocked" of every permanent (Spined Sliver, Berserk Murlodont), once for
     * {@code attacker}: its event condition is read from the blocked creature, for the watcher's controller.
     */
    private void creatureBecomesBlocked(GameData gameData, Permanent attacker) {
        CombatTrigger watched = Triggers.creatureBecomesBlocked();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(attacker, controllerId)
                        .withTriggeringPermanentId(attacker.getId());
                for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                    if (ability.options().multiplicity() == TriggerMultiplicity.EACH_BLOCKER) {
                        continue;
                    }
                    requireEach(ability, watched);
                    PermanentPredicate filter = filter(ability, watched);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                            watcherContext(gameData, watcher, controllerId)))
                            || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, controllerId,
                            watcher.getCard().getName() + "'s becomes-blocked trigger", context, entry -> {
                                entry.setTargetId(attacker.getId());
                                recordBlockedCreature(gameData, entry, attacker);
                            }), watcher.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /** "Whenever one or more creatures block" of every permanent, once for the block. */
    private void creaturesBlock(GameData gameData, List<Pair> pairs) {
        if (pairs.isEmpty()) {
            return;
        }
        CombatTrigger watched = Triggers.creaturesBlock();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(watcher, controllerId);
                for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                    requireNoFilterOrMultiplicity(ability, watched);
                    if (!eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, controllerId,
                            watcher.getCard().getName() + "'s block trigger", context,
                            TriggeredAbilityRequest.TriggerRecord.NONE), watcher.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /**
     * The unblocked attackers' abilities ("attacks and isn't blocked" triggers when no creature is declared as a
     * blocker for it, CR 509.3g): each attacker's own "whenever this creature attacks and isn't blocked" and its Auras'
     * "whenever enchanted creature attacks and isn't blocked"; the defending player's "whenever one or more creatures
     * an opponent controls attack you and aren't blocked"; "whenever a creature you control attacks and isn't blocked"
     * of the attacking player's permanents and of the face-up cards they own in exile.
     */
    private void unblocked(GameData gameData, UUID activeId, UUID defenderId, List<Permanent> unblocked) {
        for (Permanent attacker : unblocked) {
            ownUnblocked(gameData, activeId, defenderId, attacker);
            enchantedUnblocked(gameData, defenderId, attacker);
        }
        creaturesAttackYouUnblocked(gameData, activeId, defenderId, unblocked);
        creatureYouControlAttacksUnblocked(gameData, activeId, unblocked);
        exileUnblocked(gameData, activeId, unblocked);
    }

    /**
     * "Whenever this creature attacks and isn't blocked" of {@code attacker}: the event condition ("attacks a player
     * and isn't blocked", Master of Cruelties) read against its attack target. The defending player is the entry's
     * non-targeting target, triggering, attacked and defending player.
     */
    private void ownUnblocked(GameData gameData, UUID activeId, UUID defenderId, Permanent attacker) {
        CombatTrigger watched = Triggers.attacksUnblocked();
        ConditionContext context = ConditionContext.forPermanent(attacker, activeId)
                .withTargetId(attacker.getAttackTarget()).withTriggeringPermanentId(attacker.getId());
        for (TriggeredAbility ability : abilities(gameData, attacker, watched)) {
            requirePlain(ability, watched);
            if (!eventConditionMet(gameData, ability, context)) {
                continue;
            }
            put(gameData, new TriggeredAbilityRequest(ability, attacker.getCard(), attacker, activeId,
                    attacker.getCard().getName() + "'s unblocked-attack trigger", context, entry -> {
                        entry.setTargetId(defenderId);
                        entry.setNonTargeting(true);
                        entry.setTriggeringPlayerId(defenderId);
                        entry.setSourcePermanentSnapshot(new Permanent(attacker));
                        entry.setAttackedTargetId(defenderId);
                        entry.setDefendingPlayerId(defenderId);
                    }), attacker.getCard(), "'s unblocked-attack ability triggers.");
        }
    }

    /**
     * "Whenever enchanted creature attacks and isn't blocked" of the Auras attached to {@code attacker} (Cloak of
     * Confusion, Farrel's Mantle): the Aura is the source (CR 113.7), "it" the enchanted attacker.
     */
    private void enchantedUnblocked(GameData gameData, UUID defenderId, Permanent attacker) {
        CombatTrigger watched = Triggers.enchantedCreatureAttacksUnblocked();
        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !attacker.getId().equals(aura.getAttachedTo())) {
                return;
            }
            ConditionContext context = ConditionContext.forPermanent(attacker, auraControllerId)
                    .withTargetId(attacker.getAttackTarget()).withTriggeringPermanentId(attacker.getId());
            for (TriggeredAbility ability : abilities(gameData, aura, watched)) {
                requirePlain(ability, watched);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, aura.getCard(), aura, auraControllerId,
                        aura.getCard().getName() + "'s unblocked-attack trigger", context, entry -> {
                            entry.setTargetId(defenderId);
                            entry.setNonTargeting(true);
                            entry.setAttackedTargetId(defenderId);
                            entry.setTriggeringPermanentId(attacker.getId());
                            entry.setAttachedPermanentSnapshot(new Permanent(attacker));
                            entry.setSourcePermanentSnapshot(new Permanent(aura));
                            entry.setTriggeringPermanentControllerId(
                                    gameQueryService.findPermanentController(gameData, attacker.getId()));
                        }), aura.getCard(), "'s ability triggers.");
            }
        });
    }

    /**
     * "Whenever one or more creatures an opponent controls attack you and aren't blocked", when one attacks the
     * defending player directly: "that player" is the attacking player.
     */
    private void creaturesAttackYouUnblocked(GameData gameData, UUID activeId, UUID defenderId,
                                             List<Permanent> unblocked) {
        if (activeId.equals(defenderId)
                || unblocked.stream().noneMatch(attacker -> defenderId.equals(attacker.getAttackTarget()))) {
            return;
        }
        CombatTrigger watched = Triggers.creaturesAttackYouUnblocked();
        for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(defenderId, List.of()))) {
            ConditionContext context = ConditionContext.forPermanent(permanent, defenderId).withTargetId(activeId);
            for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                requirePlain(ability, watched);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent, defenderId,
                        permanent.getCard().getName() + "'s unblocked-attack trigger", context, entry -> {
                            entry.setTargetId(activeId);
                            entry.setNonTargeting(true);
                        }), permanent.getCard(), "'s ability triggers.");
            }
        }
    }

    /**
     * "Whenever a creature you control attacks and isn't blocked" of the attacking player's permanents, for each
     * unblocked attacker its filter admits (Stinkdrinker Bandit): "it" is the unblocked creature.
     */
    private void creatureYouControlAttacksUnblocked(GameData gameData, UUID activeId, List<Permanent> unblocked) {
        CombatTrigger watched = Triggers.creatureYouControlAttacksUnblocked();
        for (Permanent attacker : unblocked) {
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(activeId, List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(permanent, activeId)
                        .withTriggeringPermanentId(attacker.getId());
                for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                    requireEach(ability, watched);
                    if (ability.options().limit() != TriggerLimit.NONE) {
                        throw new IllegalStateException("No " + watched + " ability is written with a limit: "
                                + ability);
                    }
                    PermanentPredicate filter = filter(ability, watched);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker,
                            filter)) || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent, activeId,
                            permanent.getCard().getName() + "'s unblocked-attacker trigger", context, entry -> {
                                entry.setTargetId(attacker.getId());
                                entry.setNonTargeting(true);
                                entry.setTriggeringPermanentId(attacker.getId());
                            }), permanent.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /**
     * "When a [filter] creature you control attacks and isn't blocked" of the face-up cards the attacking player owns
     * in exile (Senu), for each unblocked attacker its filter admits.
     */
    private void exileUnblocked(GameData gameData, UUID activeId, List<Permanent> unblocked) {
        if (unblocked.isEmpty()) {
            return;
        }
        CombatTrigger watched = Triggers.creatureYouControlAttacksUnblocked();
        for (ExiledCardEntry exiled : List.copyOf(gameData.exiledCards)) {
            if (!activeId.equals(exiled.ownerId()) || exiled.faceDown()) {
                continue;
            }
            Card card = exiled.card();
            ConditionContext context = ConditionContext.forCard(card, activeId);
            List<TriggeredAbility> abilities = triggerIndex.cardAbilities(gameData, card).stream()
                    .filter(ability -> TriggerMatcher.combatCondition(ability, watched, Zone.EXILE) != null)
                    .toList();
            for (Permanent attacker : unblocked) {
                for (TriggeredAbility ability : abilities) {
                    requireEach(ability, watched);
                    if (ability.options().limit() != TriggerLimit.NONE) {
                        throw new IllegalStateException("No " + watched + " ability is written with a limit: "
                                + ability);
                    }
                    PermanentPredicate filter = filter(ability, watched);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker,
                            filter)) || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, card, null, activeId,
                            card.getName() + "'s unblocked-attacker trigger", context, entry -> {
                                entry.setTargetId(attacker.getId());
                                entry.setNonTargeting(true);
                                entry.setAttackedTargetId(attacker.getAttackTarget());
                                entry.setTriggeringPermanentId(attacker.getId());
                            }), card, "'s unblocked-attacker ability triggers.");
                }
            }
        }
    }
}
