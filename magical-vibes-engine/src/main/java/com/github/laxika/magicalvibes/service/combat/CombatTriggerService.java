package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.service.trigger.CombatAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.trigger.CombatWatch;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles combat trigger collection helpers shared across attack, block, and damage phases.
 * Provides aura/equipment trigger checking and APNAP trigger reordering.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatTriggerService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    /**
     * The attached permanent {@code perm}'s abilities that {@code watch} reads, each one object on the stack (CR 603.3):
     * its own "whenever enchanted / equipped creature attacks, blocks or becomes blocked" abilities and the ones it
     * grants the creature it's attached to. A "blocks" or "becomes blocked" ability may have a filter on the creature on
     * the other side of the block ("blocks or becomes blocked by a Vampire", Wooden Stake; CR 509.3f), which the
     * collector judges as the ability triggers. No Aura or Equipment writes another filter or a limit, and this throws
     * on one.
     */
    private static List<TriggeredAbility> attachedCombatAbilities(Permanent perm, CombatWatch watch) {
        List<TriggeredAbility> abilities = WatchedAbilities.printed(perm.getCard(), watch);
        boolean blockWatch = watch == CombatWatch.ON_BLOCK || watch == CombatWatch.ON_BECOMES_BLOCKED;
        for (TriggeredAbility ability : abilities) {
            if ((CombatAbilityReader.filter(ability, watch.trigger()) != null && !blockWatch)
                    || ability.options().limit() != TriggerLimit.NONE) {
                throw new IllegalStateException("The attached-permanent combat collector reads no filter or limit: "
                        + ability);
            }
        }
        return abilities;
    }

    private boolean isOwnAttackTrigger(Permanent perm, CombatWatch slot) {
        return slot == CombatWatch.ON_ATTACK
                && (perm.getCard().isAttackTriggerOwnedByAura()
                || (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                && !perm.getCard().isAttackTriggerGrantedByEquipment()));
    }

    /**
     * Checks attached permanents (auras/equipment) for the abilities the given watch reads
     * on the specified creature. Queues targeted triggers for target selection,
     * pushes non-targeted triggers directly onto the stack.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, CombatWatch slot) {
        checkAuraTriggersForCreature(gameData, creature, slot, null);
    }

    /**
     * Overload that accepts the combat opponent: the creature the attached creature blocks, which a "blocks" ability's
     * filter is judged against (Wooden Stake's "blocks … a Vampire") and which the entry records as "that creature", its
     * non-targeting target, unless the card declares a target the effect chooses. Without one, a "becomes
     * blocked" ability's filter is judged against every creature blocking it ("becomes blocked by one or more colorless
     * creatures", Hedron Blade).
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, CombatWatch slot,
                                              Permanent combatOpponent) {
        checkAuraTriggersForCreature(gameData, creature, slot, combatOpponent, null);
    }

    /**
     * Checks attached triggers while allowing the block collector to suppress each "blocks" once each combat ability
     * of an attached permanent (CR 509.3a) after it triggered in the block declaration:
     * {@code oncePerBlockTriggeredAbilities} holds the abilities that did, each by its permanent and its
     * {@link TriggeredAbility#limitKey()}, marked only once the ability's event condition, intervening "if" and filter
     * let it trigger, so one whose filter matches only the second creature blocked triggers for that one, and two such
     * abilities of one permanent each trigger.
     *
     * <p>Each ability goes on the stack through the stack door as one object (CR 603.3), with the event recorded
     * whatever its effects are: the attached creature as its triggering permanent (with its controller, "its
     * controller", and its last-known copy), the attacked player, planeswalker or battle and the defending player, and
     * on a block ability the creature on the other side of the block as "that creature", its non-targeting target
     * (CR 115.10a). An attack ability granted to the enchanted or equipped creature (Unquenchable Fury) is that
     * creature's, so the creature is its source and its controller controls it (CR 113.7, 113.8); an Aura's or
     * Equipment's own "whenever enchanted / equipped creature attacks" ability is the attached permanent's.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, CombatWatch slot,
                                              Permanent combatOpponent,
                                              Set<String> oncePerBlockTriggeredAbilities) {
        UUID creatureControllerId = gameData.findControllerOf(creature);
        if (creatureControllerId == null) return;
        final boolean creatureLostAllAbilities = gameQueryService.hasLostAllAbilities(gameData, creature);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(creature.getId())) {
                return;
            }
            // An Aura's granted triggered ability is an ability of the enchanted creature, so losing
            // all abilities (Turn to Frog) stops it from triggering. The Aura's own attack trigger is not.
            if (creatureLostAllAbilities && perm.getCard().isAura() && !perm.getCard().isAttackTriggerOwnedByAura()) {
                return;
            }
            // The intervening "if" (CR 603.4) and event condition (CR 603.2: "attacks alone", Sigil of Valor) are read
            // from the creature, against its attack target ("if it's the first combat phase", Genji Glove; "if the
            // defending player is the monarch").
            ConditionContext triggerContext = ConditionContext.forPermanent(creature, creatureControllerId)
                    .withTargetId(slot == CombatWatch.ON_ATTACK ? creature.getAttackTarget() : null);
            boolean grantedToCreature = slot == CombatWatch.ON_ATTACK && !isOwnAttackTrigger(perm, slot);
            // "Becomes blocked by a creature" (CR 509.3d) triggers for each blocker, in
            // checkAttachedPerBlockerTriggers.
            for (TriggeredAbility ability : attachedCombatAbilities(perm, slot)) {
                TriggerMultiplicity multiplicity = ability.options().multiplicity();
                if (multiplicity == TriggerMultiplicity.EACH_BLOCKER) {
                    continue;
                }
                boolean oneOrMore = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
                String oncePerBlockKey = perm.getId() + ":" + ability.limitKey();
                if (oneOrMore && oncePerBlockTriggeredAbilities != null
                        && oncePerBlockTriggeredAbilities.contains(oncePerBlockKey)) {
                    continue;
                }
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), triggerContext)) {
                    continue;
                }
                // The filter on the creature on the other side of the block, as the creature blocks or becomes
                // blocked (CR 509.3f).
                PermanentPredicate blockFilter = CombatAbilityReader.filter(ability, slot.trigger());
                if (blockFilter != null && !matchesCombatOpponent(gameData, creature, combatOpponent, blockFilter)) {
                    continue;
                }
                TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, perm.getCard(),
                        grantedToCreature ? creature : perm, grantedToCreature ? creatureControllerId : auraOwnerId,
                        perm.getCard().getName() + "'s triggered ability", triggerContext,
                        entry -> recordAttachedCombatEvent(gameData, entry, creature, creatureControllerId,
                                slot != CombatWatch.ON_ATTACK ? combatOpponent : null,
                                grantedToCreature ? creature : perm));
                if (!triggeredAbilityStackService.put(gameData, slot != CombatWatch.ON_ATTACK ? request
                        : grantedToCreature ? request.causedBySourceAttacking() : request.causedByAttack())) {
                    continue;
                }
                if (oneOrMore && oncePerBlockTriggeredAbilities != null) {
                    oncePerBlockTriggeredAbilities.add(oncePerBlockKey);
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        });
    }

    /**
     * What an attached permanent's combat ability records of its event, whatever its effects are: {@code creature},
     * the creature it is attached to, as the triggering permanent with {@code creatureControllerId} ("its
     * controller") and its last-known copy, its attack target and the defending player, and {@code thatCreature}, the
     * creature on the other side of a block, as the non-targeting target (CR 115.10a).
     */
    private void recordAttachedCombatEvent(GameData gameData, StackEntry entry, Permanent creature,
                                           UUID creatureControllerId, Permanent thatCreature, Permanent source) {
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
     * For attached permanents (equipment/auras) with abilities that trigger per blocking creature
     * (e.g. Infiltration Lens: "Whenever equipped creature becomes blocked by a creature,
     * you may draw two cards"), puts each such ability on the stack through the stack door once for each blocker, as
     * one object (CR 603.3); two abilities are two objects (CR 603.3b). An ability with a filter on the blocker
     * ("becomes blocked by a Vampire", Wooden Stake) triggers for each blocker it matches as the blocker becomes a
     * blocking creature (CR 509.3d, 509.3f); its event condition and intervening "if" are read from the attacker
     * (CR 603.2, 603.4). "That creature" is the blocker, the entry's non-targeting target, and "it" and "its
     * controller" the attacking creature, whatever the effects are.
     */
    public void checkAttachedPerBlockerTriggers(GameData gameData, Permanent attacker,
                                                 List<BlockerAssignment> blockerAssignments,
                                                 List<Permanent> defenderBattlefield, int attackerIndex) {
        UUID controllerId = gameData.findControllerOf(attacker);
        if (controllerId == null) return;
        ConditionContext attackerContext = ConditionContext.forPermanent(attacker, controllerId);

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(attacker.getId())) {
                return;
            }
            List<TriggeredAbility> perBlockerAbilities = attachedCombatAbilities(perm, CombatWatch.ON_BECOMES_BLOCKED)
                    .stream()
                    .filter(ability -> ability.options().multiplicity() == TriggerMultiplicity.EACH_BLOCKER)
                    .toList();
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.attackerIndex() != attackerIndex) {
                    continue;
                }
                Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
                for (TriggeredAbility ability : perBlockerAbilities) {
                    PermanentPredicate filter = CombatAbilityReader.filter(ability,
                            CombatWatch.ON_BECOMES_BLOCKED.trigger());
                    if ((filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, blocker, filter))
                            || !conditionEvaluationService.eventConditionMet(gameData, ability.options(),
                            attackerContext)) {
                        continue;
                    }
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                            perm.getCard(), perm, ownerId, perm.getCard().getName() + "'s triggered ability",
                            attackerContext, entry -> {
                                entry.setTargetId(blocker.getId());
                                entry.setNonTargeting(true);
                                entry.setTriggeringPermanentId(attacker.getId());
                                entry.setTriggeringPermanentControllerId(controllerId);
                                entry.setAttachedPermanentSnapshot(new Permanent(attacker));
                            }));
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    }
                }
            }
        });
    }

    /**
     * Reorders triggered abilities added to the stack since {@code startIndex} according to APNAP
     * (Active Player, Non-Active Player) ordering per CR 603.3b.
     * <p>
     * Active player's triggers are placed on the stack first (bottom), then the non-active player's
     * triggers on top. Since the stack resolves LIFO, the non-active player's triggers resolve first.
     */
    public void reorderTriggersAPNAP(GameData gameData, int startIndex, UUID activePlayerId) {
        int totalEntries = gameData.stack.size() - startIndex;
        if (totalEntries <= 1) return;

        List<StackEntry> newEntries = new ArrayList<>(gameData.stack.subList(startIndex, gameData.stack.size()));

        List<StackEntry> apTriggers = new ArrayList<>();
        List<StackEntry> napTriggers = new ArrayList<>();
        for (StackEntry entry : newEntries) {
            if (entry.getControllerId().equals(activePlayerId)) {
                apTriggers.add(entry);
            } else {
                napTriggers.add(entry);
            }
        }

        // Only reorder if both players have triggers
        if (apTriggers.isEmpty() || napTriggers.isEmpty()) return;

        // Remove new entries and re-add in APNAP order: AP first (bottom), NAP on top
        gameData.stack.subList(startIndex, gameData.stack.size()).clear();
        gameData.stack.addAll(apTriggers);
        gameData.stack.addAll(napTriggers);
    }

    private boolean matchesCombatOpponent(GameData gameData, Permanent creature,
                                          Permanent combatOpponent,
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
        final boolean[] matches = {false};
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (!matches[0] && permanent.isBlocking()
                    && permanent.getBlockingTargetIds().contains(creature.getId())
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                matches[0] = true;
            }
        });
        return matches[0];
    }
}
