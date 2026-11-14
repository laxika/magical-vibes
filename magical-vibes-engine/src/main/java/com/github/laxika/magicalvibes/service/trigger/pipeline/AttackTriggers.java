package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Emblem;
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
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.rules.AttackEvent;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triggers the attack abilities of an {@link AttackEvent}, a declaration of attackers (CR 508.1, 508.3a), each handed
 * whole to the stack door with the record of the attack. The abilities are read from {@link TriggerIndex} as the layers
 * leave them and matched by their combat trigger condition ({@link TriggerMatcher#combatCondition}); each kind of
 * attack ability is put on the stack in its turn, as below, and the attack's caller orders the batch APNAP
 * (CR 603.3b):
 *
 * <ol>
 *   <li>"whenever this creature enlists a creature" (CR 702.154c);</li>
 *   <li>for each attacker: "whenever this creature attacks", the abilities of the Auras and Equipment attached to it
 *       that trigger on its attack, and the delayed abilities watching it;</li>
 *   <li>"whenever you attack" (battlefield, command zone, delayed), "whenever you attack a player [or
 *       planeswalker]", "whenever a creature you control attacks", the planes' attack abilities, "whenever you attack"
 *       from a graveyard and from exile;</li>
 *   <li>for each attacker: "whenever a creature attacks you [or a planeswalker you control]" (delayed, then the
 *       defending player's permanents); then "whenever one or more creatures attack you", "whenever an opponent
 *       attacks a planeswalker you control" and the emblems' "whenever a creature attacks you";</li>
 *   <li>for each attacker: "whenever a creature attacks" (permanents, graveyards, delayed);</li>
 *   <li>"whenever a player attacks [one of your opponents]".</li>
 * </ol>
 *
 * A permanent's abilities are the ones it has as the layers leave it ({@link TriggerIndex#permanentAbilities}): a
 * face-down permanent or one that lost its abilities has none of its card's (CR 708.2, 613.1f). An ability's event
 * condition (CR 603.2) is judged here; the door judges its intervening "if" (CR 603.4) and limit (CR 603.2h).
 */
@Service
public class AttackTriggers {

    private static final String FIRST_QUALIFYING_ATTACK_KEY = "firstQualifyingAttackInPhase:";

    private final TriggerIndex triggerIndex;
    private final TriggeredAbilityStackService stackService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;

    public AttackTriggers(TriggerIndex triggerIndex, @Lazy TriggeredAbilityStackService stackService,
                          @Lazy GameQueryService gameQueryService,
                          @Lazy PredicateEvaluationService predicateEvaluationService,
                          @Lazy ConditionEvaluationService conditionEvaluationService,
                          @Lazy GameLogService gameLogService) {
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
    }

    /** The attack as the abilities read it: the live attackers, in declaration order, and what each attacks. */
    private record Attack(GameData gameData, AttackEvent event, UUID playerId, List<Permanent> attackers,
                          Map<UUID, UUID> targets) {

        int count() {
            return attackers.size();
        }

        UUID targetOf(Permanent attacker) {
            return targets.get(attacker.getId());
        }

        /** The declared attackers among {@code attackers}, as they were declared. */
        List<Permanent> declaredSnapshots(List<Permanent> among) {
            List<Permanent> snapshots = new ArrayList<>();
            for (Permanent declared : event.declared()) {
                if (among.stream().anyMatch(attacker -> attacker.getId().equals(declared.getId()))) {
                    snapshots.add(new Permanent(declared));
                }
            }
            return snapshots;
        }
    }

    /** Triggers the attack abilities {@code event} triggers, in the order the class describes. */
    public void trigger(GameData gameData, AttackEvent event) {
        List<Permanent> attackers = new ArrayList<>();
        for (UUID attackerId : event.attackerIds()) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker != null) {
                attackers.add(attacker);
            }
        }
        Attack attack = new Attack(gameData, event, event.attackingPlayerId(), attackers, event.attackTargets());
        event.enlisted().forEach((attackerId, enlistedId) -> enlists(attack, attackerId, enlistedId));
        List<Permanent> declaredSnapshots = attack.declaredSnapshots(attackers);
        for (Permanent attacker : attackers) {
            ownAttack(attack, attacker, declaredSnapshots);
            attachedAttack(attack, attacker);
            delayedOwnAttack(attack, attacker);
        }
        youAttack(attack);
        commandZoneYouAttack(attack);
        if (!attackers.isEmpty()) {
            delayedYouAttack(attack);
        }
        youAttackDefender(attack);
        for (Permanent attacker : attackers) {
            creatureYouControlAttacks(attack, attacker);
        }
        for (Permanent attacker : attackers) {
            planarCreatureAttacks(attack, attacker);
        }
        planarYouAttack(attack);
        graveyardYouAttack(attack);
        if (!attackers.isEmpty()) {
            exileYouAttack(attack);
        }
        for (Permanent attacker : attackers) {
            attacksYou(attack, attacker);
        }
        creaturesAttackYou(attack);
        opponentAttacksYourPlaneswalker(attack);
        for (Permanent attacker : attackers) {
            emblemAttacksYou(attack, attacker);
        }
        for (Permanent attacker : attackers) {
            creatureAttacks(attack, attacker);
            graveyardCreatureAttacks(attack, attacker);
            delayedCreatureAttacks(attack, attacker);
        }
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
                playerAttacksYourOpponent(attack, controllerId, permanent);
                playerAttacks(attack, controllerId, permanent);
            }
        }
    }

    /**
     * The abilities of {@code permanent} with a trigger condition on the combat event {@code watched} names, as the
     * layers leave them.
     */
    private List<TriggeredAbility> abilities(GameData gameData, Permanent permanent, CombatTrigger watched) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.permanentAbilities(gameData, permanent)) {
            if (TriggerMatcher.combatCondition(ability, watched, Zone.BATTLEFIELD) != null) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    private static List<TriggeredAbility> withCondition(List<TriggeredAbility> abilities, CombatTrigger watched,
                                                        Zone zone) {
        return abilities.stream().filter(ability -> TriggerMatcher.combatCondition(ability, watched, zone) != null)
                .toList();
    }

    private static PermanentPredicate filter(TriggeredAbility ability, CombatTrigger watched, Zone zone) {
        CombatTrigger condition = TriggerMatcher.combatCondition(ability, watched, zone);
        return condition == null ? null : condition.filter();
    }

    /** Throws unless {@code ability} is written with no limit and triggers once for each event. */
    private static void requireNoLimitOrMultiplicity(TriggeredAbility ability, CombatTrigger watched) {
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE || options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No " + watched + " ability is written with " + options + ": " + ability);
        }
    }

    /** Throws unless {@code ability} is also written with no filter on the creature. */
    private static void requireNoFilterLimitOrMultiplicity(TriggeredAbility ability, CombatTrigger watched,
                                                           Zone zone) {
        requireNoLimitOrMultiplicity(ability, watched);
        if (filter(ability, watched, zone) != null) {
            throw new IllegalStateException("No " + watched + " ability is written with a filter: " + ability);
        }
    }

    private boolean eventConditionMet(GameData gameData, TriggeredAbility ability, ConditionContext context) {
        return conditionEvaluationService.eventConditionMet(gameData, ability.options(), context);
    }

    private static FilterContext sourceContext(GameData gameData, Permanent source, UUID controllerId) {
        return FilterContext.of(gameData).withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(controllerId).withSourcePermanentId(source.getId());
    }

    private boolean put(GameData gameData, TriggeredAbilityRequest request, Card logCard, String logText) {
        boolean triggered = stackService.put(gameData, request);
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(logCard, logText));
        }
        return triggered;
    }

    private Permanent snapshotAttackingSource(GameData gameData, Permanent attacker) {
        Permanent snapshot = new Permanent(attacker);
        snapshot.setLastKnownPower(gameQueryService.getEffectivePower(gameData, attacker));
        return snapshot;
    }

    /**
     * "Whenever this creature enlists a creature" (CR 702.154c) of {@code attackerId}: its filter is judged on the
     * enlisted creature, the entry's triggering permanent. Enlisting isn't attacking, so no attack caused it.
     */
    private void enlists(Attack attack, UUID attackerId, UUID enlistedId) {
        GameData gameData = attack.gameData();
        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        Permanent enlisted = gameQueryService.findPermanentById(gameData, enlistedId);
        if (attacker == null || enlisted == null) {
            return;
        }
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.enlists();
        for (TriggeredAbility ability : abilities(gameData, attacker, watched)) {
            requireNoLimitOrMultiplicity(ability, watched);
            PermanentPredicate filter = filter(ability, watched, Zone.BATTLEFIELD);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(enlisted, filter,
                    sourceContext(gameData, attacker, playerId))) {
                continue;
            }
            ConditionContext context = ConditionContext.forPermanent(attacker, playerId)
                    .withTriggeringPermanentId(enlisted.getId());
            if (!eventConditionMet(gameData, ability, context)) {
                continue;
            }
            put(gameData, new TriggeredAbilityRequest(ability, attacker.getCard(), attacker, playerId,
                    attacker.getCard().getName() + "'s enlist trigger", context, entry -> {
                        entry.setNonTargeting(true);
                        entry.setSourcePermanentSnapshot(new Permanent(attacker));
                        entry.setTriggeringPermanentId(enlisted.getId());
                        entry.setTriggeringPermanentControllerId(playerId);
                        entry.setTriggeringPermanent(enlisted);
                    }), attacker.getCard(), "'s ability triggers.");
        }
    }

    /**
     * "Whenever this creature attacks" of {@code attacker}. An Equipment's own "whenever equipped creature attacks" is
     * no ability of an animated Equipment attacking itself (Captain America's Shield). "Whenever this creature attacks
     * for the first time each turn" is its first attack this turn. The entry records the attacked player,
     * planeswalker or battle and the defending player (CR 508.5), the number of attackers as X, the attacker as it
     * attacked and its power then, and the creatures declared as attackers.
     */
    private void ownAttack(Attack attack, Permanent attacker, List<Permanent> declaredSnapshots) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.attacks();
        boolean equipmentAttacking = gameQueryService.hasEffectiveSubtype(gameData, attacker, CardSubtype.EQUIPMENT)
                && !gameQueryService.hasReconfigure(gameData, attacker);
        List<TriggeredAbility> abilities = new ArrayList<>(abilities(gameData, attacker, watched));
        if (equipmentAttacking) {
            abilities.removeIf(ability -> attacker.getCard().getTriggeredAbilities().contains(ability));
        }
        ConditionContext context = ConditionContext.forPermanent(attacker, playerId)
                .withXValue(attack.count()).withTargetId(attacker.getAttackTarget());
        UUID defendingPlayerId = gameQueryService.defendingPlayerOfAttackTarget(gameData, attacker.getAttackTarget());
        for (TriggeredAbility ability : abilities) {
            TriggerOptions options = ability.options();
            if ((options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN
                    && options.limit() != TriggerLimit.FIRST_TIME_EACH_TURN)
                    || (options.multiplicity() != TriggerMultiplicity.EACH
                    && options.multiplicity() != TriggerMultiplicity.ONE_OR_MORE)) {
                throw new IllegalStateException("No attack ability is written with " + options + ": " + ability);
            }
            if (options.limit() == TriggerLimit.FIRST_TIME_EACH_TURN && attacker.getAttacksThisTurn() > 1) {
                continue;
            }
            PermanentPredicate filter = filter(ability, watched, Zone.BATTLEFIELD);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, filter)) {
                continue;
            }
            if (!eventConditionMet(gameData, ability, context)) {
                continue;
            }
            put(gameData, new TriggeredAbilityRequest(ability, attacker.getCard(), attacker, playerId,
                    attacker.getCard().getName() + "'s attack trigger", context, entry -> {
                        entry.setXValue(attack.count());
                        entry.setAttackedTargetId(attacker.getAttackTarget());
                        entry.setDefendingPlayerId(defendingPlayerId);
                        entry.setSourcePermanentSnapshot(snapshotAttackingSource(gameData, attacker));
                        entry.setTriggeringPermanentPowerAtTrigger(gameQueryService.getEffectivePower(gameData,
                                attacker));
                        entry.setAttackingPermanentSnapshots(declaredSnapshots);
                    }).causedBySourceAttacking(), attacker.getCard(), "'s attack ability triggers.");
        }
    }

    /**
     * "Whenever enchanted (equipped) creature attacks" of the Auras and Equipment attached to {@code attacker}, and the
     * attack abilities they grant it ({@link Card#isAttackTriggerOwnedByAura()},
     * {@link Card#isAttackTriggerGrantedByEquipment()}): a granted one is the creature's, its source the creature and
     * its controller the creature's (CR 113.7, 113.8), the creature's attack causing it. Its event condition and
     * intervening "if" are read from the creature against its attack target. The entry records the creature as its
     * triggering permanent, with its controller and last-known copy, its attack target and the defending player.
     */
    void attachedAttack(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        UUID creatureControllerId = gameData.findControllerOf(attacker);
        if (creatureControllerId == null) {
            return;
        }
        boolean creatureLostAllAbilities = gameQueryService.hasLostAllAbilities(gameData, attacker);
        CombatTrigger watched = Triggers.attacks();
        gameData.forEachPermanent((auraControllerId, attached) -> {
            if (!attached.isAttached() || !attached.getAttachedTo().equals(attacker.getId())) {
                return;
            }
            Card card = attached.getCard();
            // An Aura's granted triggered ability is an ability of the enchanted creature, which losing all its
            // abilities (Turn to Frog) takes; the Aura's own attack ability isn't.
            if (creatureLostAllAbilities && card.isAura() && !card.isAttackTriggerOwnedByAura()) {
                return;
            }
            boolean grantedToCreature = !(card.isAttackTriggerOwnedByAura()
                    || (card.getSubtypes().contains(CardSubtype.EQUIPMENT)
                    && !card.isAttackTriggerGrantedByEquipment()));
            ConditionContext context = ConditionContext.forPermanent(attacker, creatureControllerId)
                    .withTargetId(attacker.getAttackTarget());
            for (TriggeredAbility ability : abilities(gameData, attached, watched)) {
                if (filter(ability, watched, Zone.BATTLEFIELD) != null
                        || ability.options().limit() != TriggerLimit.NONE) {
                    throw new IllegalStateException("No attached permanent's attack ability is written with a filter "
                            + "or limit: " + ability);
                }
                if (ability.options().multiplicity() == TriggerMultiplicity.EACH_BLOCKER
                        || !eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                Permanent source = grantedToCreature ? attacker : attached;
                TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, card, source,
                        grantedToCreature ? creatureControllerId : auraControllerId,
                        card.getName() + "'s triggered ability", context,
                        entry -> recordAttachedEvent(gameData, entry, attacker, creatureControllerId, null, source));
                put(gameData, grantedToCreature ? request.causedBySourceAttacking() : request.causedByAttack(), card,
                        "'s ability triggers.");
            }
        });
    }

    /**
     * What an attached permanent's combat ability records, whatever its effects are: {@code creature}, the creature it
     * is attached to, as the triggering permanent with {@code creatureControllerId} and its last-known copy, its attack
     * target and the defending player, and {@code thatCreature}, the creature on the other side of a block, as the
     * non-targeting target (CR 115.10a).
     */
    void recordAttachedEvent(GameData gameData, StackEntry entry, Permanent creature, UUID creatureControllerId,
                             Permanent thatCreature, Permanent source) {
        if (thatCreature != null) {
            entry.setTargetId(thatCreature.getId());
        }
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentControllerId(creatureControllerId);
        UUID attackTarget = creature.getAttackTarget();
        entry.setAttackedTargetId(attackTarget);
        entry.setDefendingPlayerId(gameQueryService.defendingPlayerOfAttackTarget(gameData, attackTarget));
        entry.setTriggeringPermanentId(creature.getId());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setAttachedPermanentSnapshot(new Permanent(creature));
        entry.rememberLastKnownPermanentCard(creature.getId(), creature.getCard());
    }

    /**
     * The delayed "whenever this creature attacks" abilities watching {@code attacker} (Okoye, Mighty and Adored): the
     * resolution that made one is its source and its controller controls it (CR 603.7d, 603.7e).
     */
    private void delayedOwnAttack(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        CombatTrigger watched = Triggers.attacks();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (TriggerMatcher.combatCondition(watcher.ability(), watched, null) == null
                    || !attacker.getId().equals(watcher.watchedPermanentId())) {
                continue;
            }
            requireNoLimitOrMultiplicity(watcher.ability(), watched);
            PermanentPredicate filter = filter(watcher.ability(), watched, null);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, filter)) {
                continue;
            }
            StackEntry delayed = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(delayedEffects(watcher)), null, attacker.getId());
            delayed.setNonTargeting(true);
            delayed.setTriggeringPermanentId(attacker.getId());
            stackService.putDelayed(gameData, delayed);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * The effects of a delayed ability, its intervening "if" wrapped around each to be judged again as it resolves
     * (CR 603.4).
     */
    private static List<CardEffect> delayedEffects(TemporaryGlobalTriggeredAbility watcher) {
        Condition interveningIf = watcher.ability().options().interveningIf();
        return watcher.ability().effects().stream()
                .map(registration -> interveningIf == null ? registration.effect()
                        : (CardEffect) new ConditionalEffect(interveningIf, registration.effect()))
                .toList();
    }

    /**
     * "Whenever you attack" of the attacking player's permanents: once for the declaration, or with "in a band" once
     * for each band of two or more (CR 702.22c). "Whenever you attack with one or more Wraiths" triggers only if a
     * declared attacker matches its filter; "they", "those creatures" and "their total power" are the matching
     * attackers as declared, and "that many" their number. "For the first time each turn" counts the player's
     * qualifying attacks, not each source's.
     */
    private void youAttack(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.youAttack();
        Map<UUID, List<Permanent>> bands = new LinkedHashMap<>();
        for (Permanent attacker : attack.attackers()) {
            if (attacker.getBandId() != null) {
                bands.computeIfAbsent(attacker.getBandId(), ignored -> new ArrayList<>()).add(attacker);
            }
        }
        record Source(Permanent permanent, TriggeredAbility ability, List<Permanent> band) {
        }
        List<Source> sources = new ArrayList<>();
        for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(playerId, List.of()))) {
            for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                if (ability.options().multiplicity() == TriggerMultiplicity.EACH_BAND) {
                    for (List<Permanent> members : bands.values()) {
                        if (members.size() >= 2) {
                            sources.add(new Source(permanent, ability, List.copyOf(members)));
                        }
                    }
                } else {
                    sources.add(new Source(permanent, ability, null));
                }
            }
        }
        for (Source source : sources) {
            Permanent permanent = source.permanent();
            TriggeredAbility ability = source.ability();
            TriggerOptions options = ability.options();
            PermanentPredicate attackerFilter = filter(ability, watched, Zone.BATTLEFIELD);
            List<Permanent> declared = source.band() != null ? source.band() : attack.attackers();
            List<Permanent> matching = attackerFilter == null ? declared : declared.stream()
                    .filter(attacker -> predicateEvaluationService.matchesPermanentPredicate(attacker, attackerFilter,
                            sourceContext(gameData, permanent, playerId)))
                    .toList();
            if (matching.isEmpty()) {
                continue;
            }
            boolean firstTimeForPlayer = options.limit() == TriggerLimit.FIRST_TIME_EACH_TURN;
            if ((options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN
                    && options.limit() != TriggerLimit.ONCE && !firstTimeForPlayer)
                    || (options.multiplicity() != TriggerMultiplicity.EACH
                    && options.multiplicity() != TriggerMultiplicity.EACH_BAND)) {
                throw new IllegalStateException("No \"whenever you attack\" ability is written with " + options + ": "
                        + ability);
            }
            ConditionContext context = ConditionContext.forPermanent(permanent, playerId).withXValue(matching.size());
            if (!eventConditionMet(gameData, ability, context)
                    || (firstTimeForPlayer && !firstQualifyingAttackThisTurn(gameData, playerId))) {
                continue;
            }
            boolean sourceAttacked = matching.contains(permanent);
            List<Permanent> attackingSnapshots = attack.declaredSnapshots(matching);
            TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, permanent.getCard(), permanent,
                    playerId, permanent.getCard().getName() + "'s attack trigger", context, entry -> {
                        entry.setXValue(matching.size());
                        if (attackerFilter != null) {
                            entry.setEventValue(matching.size());
                            entry.setTriggeringPermanentIds(matching.stream().map(Permanent::getId).toList());
                        }
                        entry.setAttackingPermanentSnapshots(attackingSnapshots);
                        entry.setSourcePermanentSnapshot(new Permanent(permanent));
                    });
            if (put(gameData, sourceAttacked ? request.causedBySourceAttacking() : request.causedByAttack(),
                    permanent.getCard(), "'s attack ability triggers.") && firstTimeForPlayer) {
                gameData.keyedOncePerTurnTriggersFiredThisTurn
                        .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(FIRST_QUALIFYING_ATTACK_KEY + gameData.combatPhasesThisTurn);
            }
        }
    }

    /**
     * Whether no qualifying attack by {@code playerId} was recorded earlier this turn than the current combat phase:
     * every source that triggers off the same declaration sees the same phase and triggers.
     */
    private static boolean firstQualifyingAttackThisTurn(GameData gameData, UUID playerId) {
        String current = FIRST_QUALIFYING_ATTACK_KEY + gameData.combatPhasesThisTurn;
        return gameData.keyedOncePerTurnTriggersFiredThisTurn.getOrDefault(playerId, Set.of()).stream()
                .filter(key -> key.startsWith(FIRST_QUALIFYING_ATTACK_KEY))
                .allMatch(current::equals);
    }

    /** "Whenever you attack" of the face-up cards in the attacking player's command zone (eminence). */
    private void commandZoneYouAttack(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.youAttack();
        for (Card card : List.copyOf(gameData.playerCommandZones.getOrDefault(playerId, List.of()))) {
            if (gameData.faceDownCommandZoneCards.contains(card.getId())) {
                continue;
            }
            ConditionContext context = ConditionContext.forCard(card, playerId).withXValue(attack.count());
            for (TriggeredAbility ability : withCondition(triggerIndex.cardAbilities(gameData, card), watched,
                    Zone.COMMAND)) {
                requireNoFilterLimitOrMultiplicity(ability, watched, Zone.COMMAND);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, card, null, playerId,
                        card.getName() + "'s attack trigger", context, entry -> entry.setXValue(attack.count()))
                        .causedByAttack(), card, "'s attack ability triggers.");
            }
        }
    }

    /** The delayed "whenever you attack" abilities of the attacking player (CR 603.7). */
    private void delayedYouAttack(Attack attack) {
        GameData gameData = attack.gameData();
        CombatTrigger watched = Triggers.youAttack();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (TriggerMatcher.combatCondition(watcher.ability(), watched, null) == null
                    || !watcher.controllerId().equals(attack.playerId())) {
                continue;
            }
            requireNoFilterLimitOrMultiplicity(watcher.ability(), watched, null);
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", delayedEffects(watcher));
            entry.setNonTargeting(true);
            stackService.putMade(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * "Whenever you attack a player" once for each player attacked, and "whenever you attack a player or
     * planeswalker" once for each player and planeswalker attacked (battles aren't, CR 508.1b): the event condition
     * and the record read the attacked player or planeswalker and the number of creatures attacking it.
     */
    private void youAttackDefender(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        Set<UUID> players = new LinkedHashSet<>();
        Set<UUID> planeswalkers = new LinkedHashSet<>();
        for (Permanent attacker : attack.attackers()) {
            UUID target = attack.targetOf(attacker);
            if (gameData.playerIds.contains(target)) {
                players.add(target);
            }
        }
        for (Permanent attacker : attack.attackers()) {
            UUID target = attack.targetOf(attacker);
            Permanent attacked = gameData.playerIds.contains(target) ? null
                    : gameQueryService.findPermanentById(gameData, target);
            if (attacked != null && gameQueryService.isPlaneswalker(gameData, attacked)) {
                planeswalkers.add(target);
            }
        }
        List<Map.Entry<CombatTrigger, UUID>> defenders = new ArrayList<>();
        for (UUID attacked : players) {
            defenders.add(Map.entry(Triggers.youAttackPlayer(), attacked));
            defenders.add(Map.entry(Triggers.youAttackPlayerOrPlaneswalker(), attacked));
        }
        for (UUID attacked : planeswalkers) {
            defenders.add(Map.entry(Triggers.youAttackPlayerOrPlaneswalker(), attacked));
        }
        for (Map.Entry<CombatTrigger, UUID> defender : defenders) {
            CombatTrigger watched = defender.getKey();
            UUID attackedId = defender.getValue();
            int count = (int) attack.attackers().stream()
                    .filter(attacker -> attackedId.equals(attack.targetOf(attacker))).count();
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(playerId, List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(permanent, playerId)
                        .withTargetId(attackedId).withXValue(count);
                for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                    requireNoFilterLimitOrMultiplicity(ability, watched, Zone.BATTLEFIELD);
                    if (!eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent, playerId,
                            permanent.getCard().getName() + "'s attack trigger", context, entry -> {
                                entry.setEventValue(count);
                                entry.setAttackedTargetId(attackedId);
                                entry.setNonTargeting(true);
                                entry.setSourcePermanentSnapshot(new Permanent(permanent));
                            }).causedByAttack(), permanent.getCard(), "'s attack ability triggers.");
                }
            }
        }
    }

    /**
     * "Whenever a creature you control attacks" of the attacking player's permanents, for {@code attacker}: the filter
     * on the attacker ("a creature you control with a +1/+1 counter on it attacks", Rage Forger), the event condition
     * ("attacks alone", exalted, CR 702.83a). The entry records the attacked player, planeswalker or battle, the
     * defending player and the attacker as its triggering permanent and non-targeting "that creature".
     */
    private void creatureYouControlAttacks(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.creatureYouControlAttacks();
        for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(playerId, List.of()))) {
            ConditionContext context = ConditionContext.forPermanent(permanent, playerId)
                    .withTriggeringPermanentId(attacker.getId());
            for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                requireNoLimitOrMultiplicity(ability, watched);
                PermanentPredicate filter = filter(ability, watched, Zone.BATTLEFIELD);
                if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                        sourceContext(gameData, permanent, playerId))) {
                    continue;
                }
                if (eventConditionMet(gameData, ability, context)) {
                    putCreatureYouControlAttacks(gameData, playerId, permanent, attacker, ability, context);
                }
            }
        }
    }

    /**
     * Puts one "whenever a creature you control attacks" ability of {@code source}, triggered by {@code attacker},
     * through the stack door: the attacked player, planeswalker or battle, the defending player and the attacker as
     * the triggering permanent and non-targeting "that creature" are its record. A granted exalted ability goes on the
     * stack this way too.
     */
    public void putCreatureYouControlAttacks(GameData gameData, UUID playerId, Permanent source, Permanent attacker,
                                             TriggeredAbility ability, ConditionContext context) {
        UUID attackedTargetId = attacker.getAttackTarget();
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, source.getCard(), source, playerId,
                source.getCard().getName() + "'s attack trigger", context, entry -> {
                    entry.setSourcePermanentSnapshot(new Permanent(source));
                    entry.setAttackedTargetId(attackedTargetId);
                    entry.setDefendingPlayerId(gameQueryService.defendingPlayerOfAttackTarget(gameData,
                            attackedTargetId));
                    entry.setTargetId(attacker.getId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringPermanentId(attacker.getId());
                    entry.setTriggeringPermanentOwnerId(gameData.defaultControllerOf(attacker.getId()));
                });
        put(gameData, source == attacker ? request.causedBySourceAttacking() : request.causedByAttack(),
                source.getCard(), "'s ability triggers.");
    }

    /**
     * The face-up planes' "whenever a creature attacks" and, for the planar controller's creatures, "whenever a
     * creature you control attacks" (CR 901.6), for {@code attacker}: the attacker is the entry's non-targeting "that
     * creature" and triggering permanent, with the attacked player, planeswalker or battle.
     */
    private void planarCreatureAttacks(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || attacker.getAttackTarget() == null) {
            return;
        }
        UUID attackerControllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            Card card = object.getCard();
            List<Map.Entry<TriggeredAbility, CombatTrigger>> abilities = new ArrayList<>();
            for (TriggeredAbility ability : withCondition(card.getTriggeredAbilities(), Triggers.creatureAttacks(),
                    null)) {
                abilities.add(Map.entry(ability, Triggers.creatureAttacks()));
            }
            if (planarControllerId.equals(attackerControllerId)) {
                for (TriggeredAbility ability : withCondition(card.getTriggeredAbilities(),
                        Triggers.creatureYouControlAttacks(), null)) {
                    abilities.add(Map.entry(ability, Triggers.creatureYouControlAttacks()));
                }
            }
            FilterContext filterContext = FilterContext.of(gameData).withSourceCardId(card.getId())
                    .withSourceControllerId(planarControllerId).withSourcePermanentId(attacker.getId());
            ConditionContext context = ConditionContext.forCard(card, planarControllerId)
                    .withTriggeringPermanentId(attacker.getId());
            for (Map.Entry<TriggeredAbility, CombatTrigger> read : abilities) {
                TriggeredAbility ability = read.getKey();
                requireNoLimitOrMultiplicity(ability, read.getValue());
                PermanentPredicate filter = filter(ability, read.getValue(), null);
                if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                        filterContext)) || !eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                PlanarObject plane = object.copy();
                Permanent attackerSnapshot = new Permanent(attacker);
                put(gameData, new TriggeredAbilityRequest(ability, card, null, planarControllerId,
                        card.getName() + "'s ability", context, entry -> {
                            entry.setTargetId(attacker.getId());
                            entry.setNonTargeting(true);
                            entry.setAttackedTargetId(attacker.getAttackTarget());
                            entry.setTriggeringPermanentId(attacker.getId());
                            entry.setTriggeringPermanentControllerId(attackerControllerId);
                            entry.setSourcePermanentSnapshot(attackerSnapshot);
                            entry.setSourcePlanarObject(plane);
                        }).causedByAttack(), card, "'s ability triggers.");
            }
        }
    }

    /** The face-up planes' "whenever you attack", when the planar controller attacks (CR 901.6). */
    private void planarYouAttack(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        if (attack.count() <= 0 || gameData.planechase == null || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(playerId)) {
            return;
        }
        CombatTrigger watched = Triggers.youAttack();
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            Card card = object.getCard();
            ConditionContext context = ConditionContext.forCard(card, playerId).withXValue(attack.count());
            for (TriggeredAbility ability : withCondition(card.getTriggeredAbilities(), watched, null)) {
                requireNoFilterLimitOrMultiplicity(ability, watched, null);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                PlanarObject plane = object.copy();
                List<Permanent> attacking = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .filter(Permanent::isAttacking).toList();
                if (stackService.put(gameData, new TriggeredAbilityRequest(ability, card, null, playerId,
                        card.getName() + "'s ability", context, entry -> {
                            entry.setXValue(attack.count());
                            entry.setSourcePlanarObject(plane);
                            entry.setNonTargeting(true);
                            entry.setAttackingPermanentSnapshots(attacking);
                        }).causedByAttack())) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                }
            }
        }
    }

    /**
     * "Whenever you attack" of the cards in the attacking player's graveyard: X is the number of attackers, and the
     * entry keeps the card's graveyard stay (CR 400.7).
     */
    private void graveyardYouAttack(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.youAttack();
        for (Card card : List.copyOf(gameData.playerGraveyards.getOrDefault(playerId, List.of()))) {
            ConditionContext context = ConditionContext.forCard(card, playerId).withXValue(attack.count());
            for (TriggeredAbility ability : withCondition(triggerIndex.graveyardAbilities(gameData, card), watched,
                    Zone.GRAVEYARD)) {
                requireNoFilterLimitOrMultiplicity(ability, watched, Zone.GRAVEYARD);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                put(gameData, new TriggeredAbilityRequest(ability, card, null, playerId,
                        card.getName() + "'s graveyard attack trigger", context, entry -> {
                            entry.setXValue(attack.count());
                            entry.setSourceGraveyardEntryVersion(graveyardVersion);
                        }).causedByAttack(), card, "'s graveyard attack ability triggers.");
            }
        }
    }

    /** "Whenever you attack" of the face-up cards the attacking player owns in exile, with the number of attackers. */
    private void exileYouAttack(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.youAttack();
        for (ExiledCardEntry exiled : List.copyOf(gameData.exiledCards)) {
            if (exiled.faceDown() || !playerId.equals(exiled.ownerId())) {
                continue;
            }
            Card card = exiled.card();
            ConditionContext context = ConditionContext.forCard(card, playerId).withXValue(attack.count());
            for (TriggeredAbility ability : withCondition(triggerIndex.cardAbilities(gameData, card), watched,
                    Zone.EXILE)) {
                requireNoFilterLimitOrMultiplicity(ability, watched, Zone.EXILE);
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, card, null, playerId,
                        card.getName() + "'s exile attack trigger", context, entry -> entry.setXValue(attack.count()))
                        .causedByAttack(), card, "'s exile attack ability triggers.");
            }
        }
    }

    /**
     * For {@code attacker}: the attacked player's delayed "whenever a creature attacks you [or Tasha]" abilities, then
     * the attacked player's permanents' "whenever a creature attacks you or a planeswalker you control", "whenever a
     * creature attacks you" (a player attacked directly) and "whenever a creature an opponent controls attacks, if
     * you're the defending player" (CR 508.5). The attacker is "that creature", the entry's non-targeting target and
     * triggering permanent.
     */
    private void attacksYou(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        UUID attackedTargetId = attack.targetOf(attacker);
        Permanent attackedPermanent = gameData.playerIds.contains(attackedTargetId) ? null
                : gameQueryService.findPermanentById(gameData, attackedTargetId);
        boolean battleAttacked = attackedPermanent != null && gameQueryService.isBattle(gameData, attackedPermanent);
        UUID attackedPlayerId = gameData.playerIds.contains(attackedTargetId) ? attackedTargetId
                : attackedPermanent != null && battleAttacked ? attackedPermanent.getProtectorPlayerId()
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (attackedPlayerId == null) {
            return;
        }
        if (!battleAttacked) {
            delayedAttacksYou(gameData, attacker, attackedPlayerId, attackedTargetId);
        }
        List<CombatTrigger> watches = new ArrayList<>();
        if (!battleAttacked) {
            watches.add(Triggers.creatureAttacksYouOrYourPlaneswalker());
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            watches.add(Triggers.creatureAttacksYou());
        }
        watches.add(Triggers.creatureAttacksWhileYouDefend());
        for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(attackedPlayerId,
                List.of()))) {
            ConditionContext context = ConditionContext.forPermanent(permanent, attackedPlayerId)
                    .withTriggeringPermanentId(attacker.getId()).withTargetId(attackedTargetId);
            for (CombatTrigger watched : watches) {
                for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                    requireNoLimitOrMultiplicity(ability, watched);
                    PermanentPredicate filter = filter(ability, watched, Zone.BATTLEFIELD);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                            sourceContext(gameData, permanent, attackedPlayerId)))
                            || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent,
                            attackedPlayerId, permanent.getCard().getName() + "'s trigger", context, entry -> {
                                entry.setTargetId(attacker.getId());
                                entry.setNonTargeting(true);
                                entry.setTriggeringPermanentId(attacker.getId());
                                entry.setTriggeringPermanentControllerId(playerId);
                                entry.setSourcePermanentSnapshot(new Permanent(permanent));
                            }).causedByAttack(), permanent.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /**
     * The attacked player's delayed "whenever a creature attacks you" abilities; one watching a permanent ("attacks you
     * or Tasha") triggers only for an attack on the player or that permanent.
     */
    private void delayedAttacksYou(GameData gameData, Permanent attacker, UUID attackedPlayerId,
                                   UUID attackedTargetId) {
        CombatTrigger watched = Triggers.creatureAttacksYouOrYourPlaneswalker();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (TriggerMatcher.combatCondition(watcher.ability(), watched, null) == null
                    || !watcher.controllerId().equals(attackedPlayerId)) {
                continue;
            }
            if (watcher.watchedPermanentId() != null && !watcher.watchedPermanentId().equals(attackedTargetId)
                    && !attackedPlayerId.equals(attackedTargetId)) {
                continue;
            }
            requireNoFilterLimitOrMultiplicity(watcher.ability(), watched, null);
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", delayedEffects(watcher), attacker.getId(),
                    attacker.getId());
            entry.setTriggeringPermanentId(attacker.getId());
            entry.setSourcePermanentSnapshot(new Permanent(attacker));
            entry.setNonTargeting(true);
            stackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * "Whenever one or more creatures attack you", once for each player attacked directly: "those creatures" are the
     * ones attacking that player, as they were declared.
     */
    private void creaturesAttackYou(Attack attack) {
        GameData gameData = attack.gameData();
        CombatTrigger watched = Triggers.creaturesAttackYou();
        Set<UUID> attackedPlayers = new LinkedHashSet<>();
        for (Permanent attacker : attack.attackers()) {
            if (gameData.playerIds.contains(attack.targetOf(attacker))) {
                attackedPlayers.add(attack.targetOf(attacker));
            }
        }
        for (UUID attackedPlayerId : attackedPlayers) {
            List<Permanent> attackingYou = attack.declaredSnapshots(attack.attackers().stream()
                    .filter(attacker -> attackedPlayerId.equals(attack.targetOf(attacker))).toList());
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(attackedPlayerId,
                    List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(permanent, attackedPlayerId);
                for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                    requireNoFilterLimitOrMultiplicity(ability, watched, Zone.BATTLEFIELD);
                    if (!eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent,
                            attackedPlayerId, permanent.getCard().getName() + "'s trigger", context, entry -> {
                                entry.setNonTargeting(true);
                                entry.setActivePlayerId(attack.playerId());
                                entry.setSourcePermanentSnapshot(new Permanent(permanent));
                                entry.setAttackingPermanentSnapshots(attackingYou);
                            }).causedByAttack(), permanent.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /**
     * "Whenever an opponent attacks a planeswalker you control with one or more creatures", once for each planeswalker
     * attacked: "that player" is the attacking player.
     */
    private void opponentAttacksYourPlaneswalker(Attack attack) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.opponentAttacksYourPlaneswalker();
        Set<UUID> planeswalkers = new LinkedHashSet<>();
        for (Permanent attacker : attack.attackers()) {
            UUID target = attack.targetOf(attacker);
            if (gameData.playerIds.contains(target)) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, target);
            if (controllerId == null || controllerId.equals(playerId)) {
                continue;
            }
            Permanent attacked = gameQueryService.findPermanentById(gameData, target);
            if (attacked != null && gameQueryService.isPlaneswalker(gameData, attacked)) {
                planeswalkers.add(target);
            }
        }
        for (UUID planeswalkerId : planeswalkers) {
            UUID attackedPlayerId = gameQueryService.findPermanentController(gameData, planeswalkerId);
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(attackedPlayerId,
                    List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(permanent, attackedPlayerId);
                for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                    requireNoFilterLimitOrMultiplicity(ability, watched, Zone.BATTLEFIELD);
                    if (!eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent,
                            attackedPlayerId, permanent.getCard().getName() + "'s trigger", context, entry -> {
                                entry.setTargetId(playerId);
                                entry.setNonTargeting(true);
                            }).causedByAttack(), permanent.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /**
     * The emblems' "whenever a creature attacks you" (Garruk, Apex Predator's), for {@code attacker} attacking the
     * emblem's controller directly. An emblem's ability functions in the command zone (CR 114.4); one written with a
     * relation, filter or option its reader doesn't judge fails loudly.
     */
    private void emblemAttacksYou(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        UUID attackedTargetId = attack.targetOf(attacker);
        if (gameData.emblems.isEmpty() || attackedTargetId == null) {
            return;
        }
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!emblem.controllerId().equals(attackedTargetId)) {
                continue;
            }
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                boolean attacksYou = false;
                for (var condition : ability.conditions()) {
                    if (!(condition.trigger() instanceof CombatTrigger trigger)
                            || trigger.event() != CombatTrigger.CombatEvent.ATTACKS_YOU) {
                        continue;
                    }
                    if (!trigger.equals(Triggers.creatureAttacksYou())
                            || !ability.options().equals(TriggerOptions.DEFAULT)) {
                        throw new IllegalStateException(emblemName(emblem) + " has an attack ability no reader "
                                + "judges: " + ability);
                    }
                    attacksYou = true;
                }
                if (!attacksYou) {
                    continue;
                }
                Card source = emblem.sourceCard() != null ? emblem.sourceCard() : attacker.getCard();
                String description = emblemName(emblem);
                stackService.put(gameData, new TriggeredAbilityRequest(ability, source, null, emblem.controllerId(),
                        description, ConditionContext.forCard(source, emblem.controllerId())
                        .withTriggeringPermanentId(attacker.getId()), entry -> {
                            entry.setMadeByResolution(true);
                            entry.setTriggeringPermanentId(attacker.getId());
                            entry.setTriggeringPermanentControllerId(attack.playerId());
                            entry.setTriggeringCardId(attacker.getCard().getId());
                            entry.setNonTargeting(true);
                        }).ofEmblem());
                gameLogService.append(gameData, GameLog.text(description + " triggers for "
                        + attacker.getCard().getName() + " attacking."));
            }
        }
    }

    private static String emblemName(Emblem emblem) {
        return (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem") + "'s emblem";
    }

    /**
     * "Whenever a creature attacks" of every permanent, for {@code attacker}, whoever controls it and whomever it
     * attacks (Caltrops): its filter ("an equipped creature you control", Nahiri, Forged in Fury) is judged from the
     * ability's controller. The attacker is "it", the entry's non-targeting target and triggering permanent.
     */
    private void creatureAttacks(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        CombatTrigger watched = Triggers.creatureAttacks();
        UUID attackedTargetId = attacker.getAttackTarget();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId,
                    List.of()))) {
                ConditionContext context = ConditionContext.forPermanent(permanent, controllerId)
                        .withTriggeringPermanentId(attacker.getId()).withTargetId(attackedTargetId);
                for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
                    requireNoLimitOrMultiplicity(ability, watched);
                    PermanentPredicate filter = filter(ability, watched, Zone.BATTLEFIELD);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                            sourceContext(gameData, permanent, controllerId)))
                            || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, permanent.getCard(),
                            permanent, controllerId, permanent.getCard().getName() + "'s trigger", context, entry -> {
                                entry.setSourcePermanentSnapshot(new Permanent(permanent));
                                entry.setAttackingPermanentSnapshots(List.of(new Permanent(attacker)));
                                entry.setTargetId(attacker.getId());
                                entry.setNonTargeting(true);
                                entry.setAttackedTargetId(attacker.getAttackTarget());
                                entry.setTriggeringPermanentId(attacker.getId());
                                entry.setTriggeringPermanentControllerId(
                                        gameQueryService.findPermanentController(gameData, attacker.getId()));
                            });
                    put(gameData, permanent == attacker ? request.causedBySourceAttacking() : request.causedByAttack(),
                            permanent.getCard(), "'s ability triggers.");
                }
            }
        }
    }

    /**
     * "Whenever a creature attacks" from a graveyard: "whenever your commander attacks" (Vivi's Persistence), filtered
     * to a commander the card's owner owns, triggering once for each such attacker from its owner's graveyard.
     */
    private void graveyardCreatureAttacks(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        CombatTrigger watched = Triggers.creatureAttacks();
        for (UUID ownerId : gameData.orderedPlayerIds) {
            for (Card card : List.copyOf(gameData.playerGraveyards.getOrDefault(ownerId, List.of()))) {
                FilterContext filterContext = FilterContext.of(gameData).withSourceCardId(card.getId())
                        .withSourceControllerId(ownerId);
                ConditionContext context = ConditionContext.forCard(card, ownerId)
                        .withTriggeringPermanentId(attacker.getId()).withTargetId(attacker.getAttackTarget());
                for (TriggeredAbility ability : withCondition(triggerIndex.graveyardAbilities(gameData, card),
                        watched, Zone.GRAVEYARD)) {
                    requireNoLimitOrMultiplicity(ability, watched);
                    PermanentPredicate filter = filter(ability, watched, Zone.GRAVEYARD);
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                            filterContext)) || !eventConditionMet(gameData, ability, context)) {
                        continue;
                    }
                    long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                    put(gameData, new TriggeredAbilityRequest(ability, card, null, ownerId,
                            card.getName() + "'s graveyard attack trigger", context, entry -> {
                                entry.setXValue(1);
                                entry.setSourceGraveyardEntryVersion(graveyardVersion);
                            }).causedByAttack(), card, "'s graveyard attack ability triggers.");
                }
            }
        }
    }

    /**
     * The delayed "whenever a creature attacks this turn" abilities, for {@code attacker}: the filter from the
     * registering player's point of view, the event condition ("attacks alone", The Last Ronin) with the attacker. One
     * with a target of its own chooses it as it goes on the stack (CR 603.3d); otherwise the attacker is "it".
     */
    private void delayedCreatureAttacks(Attack attack, Permanent attacker) {
        GameData gameData = attack.gameData();
        CombatTrigger watched = Triggers.creatureAttacks();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (TriggerMatcher.combatCondition(watcher.ability(), watched, null) == null) {
                continue;
            }
            requireNoLimitOrMultiplicity(watcher.ability(), watched);
            PermanentPredicate filter = filter(watcher.ability(), watched, null);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                    FilterContext.of(gameData).withSourceControllerId(watcher.controllerId())
                            .withSourceCardId(watcher.sourceCard().getId()))) {
                continue;
            }
            if (!conditionEvaluationService.triggerConditionsMet(gameData, watcher.ability().options(),
                    ConditionContext.forPermanent(attacker, watcher.controllerId()))) {
                continue;
            }
            List<CardEffect> effects = delayedEffects(watcher);
            if (effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
                StackEntry targeted = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                        watcher.controllerId(), watcher.sourceCard().getName() + "'s ability", effects);
                targeted.setSourcePermanentSnapshot(new Permanent(attacker));
                stackService.putMade(gameData, targeted);
            } else {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                        watcher.controllerId(), watcher.sourceCard().getName() + "'s ability", effects,
                        attacker.getId(), attacker.getId());
                entry.setSourcePermanentSnapshot(new Permanent(attacker));
                entry.setNonTargeting(true);
                stackService.putDelayed(gameData, entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * "Whenever a player attacks one of your opponents" of {@code source} and, for another player's attack, "whenever
     * an opponent attacks another one of your opponents", once for each opponent attacked: "that player" is the
     * attacking player, the entry's non-targeting target, and the attacked opponent its attacked target.
     */
    private void playerAttacksYourOpponent(Attack attack, UUID controllerId, Permanent source) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        List<CombatTrigger> watches = controllerId.equals(playerId)
                ? List.of(Triggers.playerAttacksOneOfYourOpponents())
                : List.of(Triggers.playerAttacksOneOfYourOpponents(), Triggers.opponentAttacksAnotherOpponent());
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (CombatTrigger watched : watches) {
            for (TriggeredAbility ability : abilities(gameData, source, watched)) {
                requireNoFilterLimitOrMultiplicity(ability, watched, Zone.BATTLEFIELD);
                abilities.add(ability);
            }
        }
        if (abilities.isEmpty()) {
            return;
        }
        Set<UUID> attackedOpponents = new LinkedHashSet<>();
        for (Permanent attacker : attack.attackers()) {
            UUID target = attack.targetOf(attacker);
            if (gameData.playerIds.contains(target) && !controllerId.equals(target)) {
                attackedOpponents.add(target);
            }
        }
        for (UUID attackedOpponentId : attackedOpponents) {
            ConditionContext context = ConditionContext.forPermanent(source, controllerId).withTargetId(playerId)
                    .withAttackedTargetId(attackedOpponentId).withXValue(attack.count());
            for (TriggeredAbility ability : abilities) {
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, source.getCard(), source, controllerId,
                        source.getCard().getName() + "'s trigger", context, entry -> {
                            entry.setXValue(attack.count());
                            entry.setTargetId(playerId);
                            entry.setTriggeringPermanentControllerId(playerId);
                            entry.setAttackedTargetId(attackedOpponentId);
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                            entry.setNonTargeting(true);
                        }).causedByAttack(), source.getCard(), "'s ability triggers.");
            }
        }
    }

    /**
     * "Whenever a player attacks" of {@code permanent}, once for the declaration: its filter on the attackers
     * ("one or more creatures that are enchanted by an Aura you control attack", Killian), its event condition ("with
     * three or more creatures", CR 603.2). "That player" is the attacking player, the entry's non-targeting target, and
     * "those creatures" the attackers that triggered it.
     */
    private void playerAttacks(Attack attack, UUID controllerId, Permanent permanent) {
        GameData gameData = attack.gameData();
        UUID playerId = attack.playerId();
        CombatTrigger watched = Triggers.playerAttacks();
        ConditionContext context = ConditionContext.forPermanent(permanent, controllerId).withTargetId(playerId)
                .withXValue(attack.count());
        for (TriggeredAbility ability : abilities(gameData, permanent, watched)) {
            requireNoLimitOrMultiplicity(ability, watched);
            PermanentPredicate filter = filter(ability, watched, Zone.BATTLEFIELD);
            List<Permanent> triggering = filter == null ? attack.attackers() : attack.attackers().stream()
                    .filter(attacker -> predicateEvaluationService.matchesPermanentPredicate(attacker, filter,
                            sourceContext(gameData, permanent, controllerId)))
                    .toList();
            if (triggering.isEmpty() || !eventConditionMet(gameData, ability, context)) {
                continue;
            }
            List<Permanent> attackingSnapshots = attack.declaredSnapshots(triggering);
            put(gameData, new TriggeredAbilityRequest(ability, permanent.getCard(), permanent, controllerId,
                    permanent.getCard().getName() + "'s trigger", context, entry -> {
                        entry.setXValue(attack.count());
                        entry.setTargetId(playerId);
                        entry.setTriggeringPermanentControllerId(playerId);
                        entry.setNonTargeting(true);
                        entry.setSourcePermanentSnapshot(new Permanent(permanent));
                        entry.setAttackingPermanentSnapshots(attackingSnapshots);
                    }).causedByAttack(), permanent.getCard(), "'s ability triggers.");
        }
    }

    /**
     * "Whenever a creature you control attacking causes a triggered ability of that creature to trigger" (Firebender
     * Ascension) for {@code triggeredAbility}, an ability of {@code attacker} its attack made trigger: each such
     * ability of the attacker's controller's permanents goes on the stack above it, with the attacker as its triggering
     * permanent and that ability as its triggering spell ("copy that ability"). A "you may" is asked as it resolves
     * (CR 603.5).
     */
    public void triggerAttackingSourceAbility(GameData gameData, Permanent attacker, StackEntry triggeredAbility) {
        if (attacker == null || triggeredAbility == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) {
            return;
        }
        CombatTrigger watched = Triggers.attackingCreatureYouControlAbilityTriggers();
        for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
            for (TriggeredAbility ability : abilities(gameData, watcher, watched)) {
                requireNoFilterLimitOrMultiplicity(ability, watched, Zone.BATTLEFIELD);
                ConditionContext context = ConditionContext.forPermanent(watcher, controllerId)
                        .withTriggeringPermanentId(attacker.getId());
                if (!eventConditionMet(gameData, ability, context)) {
                    continue;
                }
                put(gameData, new TriggeredAbilityRequest(ability, watcher.getCard(), watcher, controllerId,
                        watcher.getCard().getName() + "'s triggered ability", context, entry -> {
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(new Permanent(watcher));
                            entry.setTriggeringPermanentId(attacker.getId());
                            entry.setTriggeringPermanentControllerId(controllerId);
                            entry.setTriggeringSpell(triggeredAbility);
                        }), watcher.getCard(), "'s ability triggers.");
            }
        }
    }
}
