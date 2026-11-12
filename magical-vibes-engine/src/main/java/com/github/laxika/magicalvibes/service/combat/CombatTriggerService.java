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

    /**
     * The attached permanent {@code perm}'s abilities that {@code watch} reads, one list of effects for each ability
     * (CR 603.3): its own "whenever
     * equipped / enchanted creature attacks, blocks or becomes blocked", or the ability it grants the creature. A
     * "blocks" or "becomes blocked" ability may have a filter on the creature on the other side of the block
     * ("blocks or becomes blocked by a Vampire", Wooden Stake; CR 509.3f), which the collector judges as the ability
     * triggers. No Aura or Equipment writes another filter or a limit, and this throws on one.
     */
    private static List<List<CombatAbilityReader.CombatEffect>> attachedCombatAbilities(Permanent perm,
                                                                                         CombatWatch watch) {
        List<List<CombatAbilityReader.CombatEffect>> abilities = CombatAbilityReader.readByAbility(
                WatchedAbilities.printed(perm.getCard(), watch), watch.trigger());
        boolean blockWatch = watch == CombatWatch.ON_BLOCK || watch == CombatWatch.ON_BECOMES_BLOCKED;
        for (List<CombatAbilityReader.CombatEffect> ability : abilities) {
            CombatAbilityReader.CombatEffect read = ability.getFirst();
            if ((read.filter() != null && !blockWatch) || read.limit() != TriggerLimit.NONE) {
                throw new IllegalStateException("The attached-permanent combat collector reads no filter or limit: "
                        + read.ability());
            }
        }
        return abilities;
    }

    /**
     * Puts {@code trigger}, the entry a block-step collector built for one ability that chooses no target, on the
     * stack; an ability whose mode is chosen as it triggers (CR 603.3c) instead queues that choice with the entry as
     * the record of the event, which the entry the chosen modes go on the stack as carries
     * ({@code StackEntry.carryTriggerRecord}). The choice builds the stack object from the chosen modes, so the modal
     * is the ability's one effect.
     */
    public void putOnStackOrChooseMode(GameData gameData, StackEntry trigger) {
        List<CardEffect> effects = trigger.getEffectsToResolve();
        ChooseOneEffect modal = effects.stream().map(TriggeredModalEffect::choiceMadeAsTriggered)
                .filter(java.util.Objects::nonNull).findFirst().orElse(null);
        if (modal == null) {
            gameData.stack.add(trigger);
            return;
        }
        gameData.handOffTriggerRecord(trigger);
        if (effects.size() != 1) {
            throw new IllegalStateException("A mode chosen as a block-step ability triggers is the ability's one "
                    + "effect: " + trigger.getCard().getName());
        }
        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                trigger.getCard(), trigger.getControllerId(), modal, trigger.getSourcePermanentId())
                .withTriggerRecord(trigger));
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
     * {@link com.github.laxika.magicalvibes.model.TriggeredAbility#limitKey()}, marked only once the ability's event
     * condition, intervening "if" and filter let it trigger, so one whose filter matches only the second creature
     * blocked triggers for that one, and two such abilities of one permanent each trigger.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, CombatWatch slot,
                                              Permanent combatOpponent,
                                              Set<String> oncePerBlockTriggeredAbilities) {
        UUID creatureControllerId = gameData.findControllerOf(creature);
        if (creatureControllerId == null) return;
        final UUID finalCreatureControllerId = creatureControllerId;
        final boolean creatureLostAllAbilities = gameQueryService.hasLostAllAbilities(gameData, creature);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())) {
                // An Aura's granted triggered ability is an ability of the enchanted creature, so losing
                // all abilities (Turn to Frog) stops it from triggering. The Aura's own attack trigger is not.
                if (creatureLostAllAbilities && perm.getCard().isAura()
                        && !perm.getCard().isAttackTriggerOwnedByAura()) {
                    return;
                }
                List<List<CombatAbilityReader.CombatEffect>> auraAbilities = attachedCombatAbilities(perm, slot);
                // Skip per-blocker effects ("becomes blocked by a creature", CR 509.3d) — they are handled by
                // checkAttachedPerBlockerTriggers. "Blocks" once each combat (CR 509.3a) triggers once for the
                // declaration, judged for each ability below.
                List<List<CombatAbilityReader.CombatEffect>> queuedAbilities = auraAbilities.stream()
                        .filter(ability -> !ability.getFirst().eachBlocker())
                        .toList();
                // Each ability is one object on the stack (CR 603.3): Reaper's Talisman's two.
                // The intervening "if" (CR 603.4) and event condition (CR 603.2: "attacks alone", Sigil of Valor) are read
                // from the creature, against its attack target ("if it's the first combat phase", Genji Glove;
                // "if the defending player is the monarch").
                ConditionContext triggerContext = ConditionContext.forPermanent(creature, finalCreatureControllerId)
                        .withTargetId(slot == CombatWatch.ON_ATTACK ? creature.getAttackTarget() : null);
                for (List<CombatAbilityReader.CombatEffect> ability : queuedAbilities) {
                    String oncePerBlockKey = perm.getId() + ":" + ability.getFirst().ability().limitKey();
                    if (ability.getFirst().oneOrMore() && oncePerBlockTriggeredAbilities != null
                            && oncePerBlockTriggeredAbilities.contains(oncePerBlockKey)) {
                        continue;
                    }
                    if (!conditionEvaluationService.triggerConditionsMet(gameData,
                            ability.getFirst().ability().options(), triggerContext)) {
                        continue;
                    }
                    // The filter on the creature on the other side of the block, as the creature blocks or becomes
                    // blocked (CR 509.3f).
                    PermanentPredicate blockFilter = ability.getFirst().filter();
                    if (blockFilter != null && !matchesCombatOpponent(gameData, creature, combatOpponent, blockFilter)) {
                        continue;
                    }
                    if (ability.getFirst().oneOrMore() && oncePerBlockTriggeredAbilities != null) {
                        oncePerBlockTriggeredAbilities.add(oncePerBlockKey);
                    }
                    List<CardEffect> effectsForStack = CombatAbilityReader.effectsAsJudged(ability);
                    if (effectsForStack.isEmpty()) continue;
                    // On a block ability the creature on the other side of the block is "that creature": a permanent
                    // target the card declares is chosen as the ability goes on the stack (CR 603.3d), and one it
                    // doesn't declare is that creature (CR 115.10a), which the entry records as its non-targeting
                    // target whatever the effects are.
                    boolean thatCreature = slot != CombatWatch.ON_ATTACK && combatOpponent != null;

                    // The entry of an ability that chooses nothing is the record of the event every route carries
                    // (StackEntry.carryTriggerRecord): a mode, a graveyard card or a target chosen as the ability goes
                    // on the stack changes nothing about what triggered it.
                    // An attack ability granted to the enchanted creature (Unquenchable Fury) is an ability
                    // of that creature, so the creature is its source and its controller controls it
                    // (CR 113.7, CR 113.8).
                    boolean grantedToCreature = slot == CombatWatch.ON_ATTACK
                            && !isOwnAttackTrigger(perm, slot);
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            grantedToCreature ? finalCreatureControllerId : auraOwnerId,
                            perm.getCard().getName() + "'s triggered ability",
                            effectsForStack,
                            thatCreature ? combatOpponent.getId() : null,
                            grantedToCreature ? creature.getId() : perm.getId()
                    );
                    // No effect of the ability targets, so nothing on its entry is checked as a target:
                    // "that creature" is its non-targeting target.
                    trigger.setNonTargeting(true);
                    // "Its controller" of the enchanted or equipped creature is the triggering permanent's,
                    // read as the ability resolves.
                    trigger.setTriggeringPermanentControllerId(finalCreatureControllerId);
                    // Bake attacked player/planeswalker so DEFENDING_PLAYER effects
                    // (e.g. equipment-granted Afflict) can resolve.
                    trigger.setAttackedTargetId(creature.getAttackTarget());
                    trigger.setDefendingPlayerId(creature.getAttackTarget() == null ? null
                            : gameData.playerIds.contains(creature.getAttackTarget())
                                    ? creature.getAttackTarget()
                                    : gameQueryService.findPermanentController(gameData, creature.getAttackTarget()));
                    trigger.setTriggeringPermanentId(creature.getId());
                    trigger.setSourcePermanentSnapshot(new Permanent(grantedToCreature ? creature : perm));
                    trigger.setAttachedPermanentSnapshot(new Permanent(creature));
                    trigger.rememberLastKnownPermanentCard(creature.getId(), creature.getCard());
                    int previousCopies = slot == CombatWatch.ON_ATTACK
                            ? beginAttackTriggerCopies(gameData, auraOwnerId, perm)
                            : -1;
                    GameData.TriggerRouting attachedRouting = gameData.beginTriggerRouting();
                    try {
                        gameData.handOffTriggerRecord(trigger);
                        if (slot == CombatWatch.ON_ATTACK && effectsForStack.size() == 1
                                && effectsForStack.getFirst() instanceof ChooseOneAtTriggerTimeEffect modal) {
                            // "Whenever equipped creature attacks, choose one —" (Pip-Boy 3000): the mode is chosen as
                            // the ability goes on the stack (CR 700.2b); "that creature" is the attacking creature, the
                            // entry's triggering permanent. The Equipment's own ability is its controller's, a granted
                            // one the creature's (CR 113.8).
                            boolean ownTrigger = isOwnAttackTrigger(perm, slot);
                            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                    perm.getCard(), ownTrigger ? auraOwnerId : finalCreatureControllerId,
                                    modal.choice(), ownTrigger ? perm.getId() : creature.getId(), false, false, null,
                                    creature.getAttackTarget(), creature.getId(), false).withTriggerRecord(trigger));
                            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                            continue;
                        }
                        if (slot == CombatWatch.ON_ATTACK && effectsForStack.stream()
                                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
                            UUID attackedTargetId = creature.getAttackTarget();
                            UUID defendingPlayerId = attackedTargetId == null ? null
                                    : gameData.playerIds.contains(attackedTargetId)
                                            ? attackedTargetId
                                            : gameQueryService.findPermanentController(gameData, attackedTargetId);
                            // The ability's controller and source are the record's: a granted ability is the
                            // enchanted creature's (CR 113.8), whatever it chooses.
                            graveyardTargetingService.handleAttackGraveyardTargeting(
                                    gameData, trigger.getControllerId(), perm.getCard(), effectsForStack,
                                    trigger.getSourcePermanentId(), defendingPlayerId, List.of(), trigger);
                            continue;
                        } else {
                            // Check if any effect needs a permanent target — queue for target selection
                            boolean needsTarget = effectsForStack.stream()
                                    .filter(e -> !thatCreature || perm.getCard().hasEffectTargetIndex(e))
                                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                            if (needsTarget) {
                                if (perm.getCard().isAura() && perm.getCard().getTargetGroups().size() > 1
                                        && effectsForStack.stream().noneMatch(effect ->
                                        perm.getCard().getEffectTargetIndex(effect) >= 0)) {
                                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                            perm.getCard(), trigger.getControllerId(), effectsForStack,
                                            trigger.getSourcePermanentId(),
                                            List.of(), 1, 0, List.of(0), 0, List.of(), false, null,
                                            creature.getId()).withTriggerRecord(trigger));
                                } else if (needsSlotBySlotTargetSelection(perm.getCard(), effectsForStack)) {
                                    int firstGroup = effectsForStack.stream()
                                            .mapToInt(perm.getCard()::getEffectTargetIndex)
                                            .filter(index -> index >= 0).min().orElse(0);
                                    List<Integer> skippedGroups = java.util.Collections.nCopies(firstGroup, 0);
                                    gameData.queueInteraction(
                                            new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                                    perm.getCard(), trigger.getControllerId(), effectsForStack,
                                                    trigger.getSourcePermanentId(),
                                                    List.of(), firstGroup, 0, skippedGroups, 0, List.of(), false,
                                                    null, creature.getId()).withTriggerRecord(trigger));
                                } else {
                                    UUID attackedTargetId = slot == CombatWatch.ON_ATTACK
                                            ? creature.getAttackTarget() : null;
                                    UUID defendingPlayerId = attackedTargetId == null ? null
                                            : gameData.playerIds.contains(attackedTargetId)
                                                    ? attackedTargetId
                                                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
                                    // An attack trigger granted by an Aura is an ability of the enchanted creature,
                                    // so its source and controller are that creature (CR 113.7, CR 113.8). An
                                    // Aura's own "whenever enchanted creature attacks" ability is controlled by the
                                    // Aura's controller and has the Aura as its source. An Equipment's "whenever
                                    // equipped creature attacks" ability is likewise the Equipment's own ability.
                                    boolean permOwnedAttackTrigger = isOwnAttackTrigger(perm, slot);
                                    UUID grantedAttackControllerId = permOwnedAttackTrigger
                                            ? auraOwnerId : finalCreatureControllerId;
                                    UUID targetChooserId = slot == CombatWatch.ON_ATTACK
                                            && perm.getCard().isCombatTriggerTargetChosenByDefendingPlayer()
                                            ? defendingPlayerId : grantedAttackControllerId;
                                    gameData.queueInteraction((
                                            slot == CombatWatch.ON_ATTACK
                                                    ? new PermanentChoiceContext.AttackTriggerTarget(
                                                            perm.getCard(), grantedAttackControllerId, effectsForStack,
                                                            permOwnedAttackTrigger ? perm.getId() : creature.getId(),
                                                            targetChooserId, attackedTargetId, creature.getId())
                                                    : new PermanentChoiceContext.AttackTriggerTarget(
                                                            perm.getCard(), auraOwnerId, effectsForStack, perm.getId(),
                                                            auraOwnerId, null, creature.getId()))
                                            .withTriggerRecord(trigger));
                                }
                                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                                log.info("Game {} - {} targeted attack trigger queued for target selection (attached to {})",
                                        gameData.id, perm.getCard().getName(), creature.getCard().getName());
                            } else {
                                putOnStackOrChooseMode(gameData, trigger);
                                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                                log.info("Game {} - {} aura trigger pushed onto stack (enchanted creature {})",
                                        gameData.id, perm.getCard().getName(), creature.getCard().getName());
                            }
                        }
                    } finally {
                        gameData.endTriggerRouting(attachedRouting);
                        if (previousCopies >= 0) {
                            gameData.restoreTriggeredAbilityCopies(previousCopies);
                        }
                    }
                }
            }
        });
    }

    private boolean needsSlotBySlotTargetSelection(Card card, List<CardEffect> effects) {
        Set<Integer> boundGroups = effects.stream()
                .map(card::getEffectTargetIndex)
                .filter(index -> index >= 0)
                .collect(java.util.stream.Collectors.toSet());
        if (boundGroups.isEmpty()) {
            return card.getTargetGroups().size() > 1
                    || card.getTargetGroups().stream().anyMatch(group -> group.getMaxTargets() > 1
                            || group.getMinTargets() == 0 || group.getDynamicMinTargets() != null)
                    || etbTokenTargetService.needsSlotBySlotTargetSelection(card);
        }
        return boundGroups.size() > 1 || card.getTargetGroups().stream()
                .filter(group -> boundGroups.contains(group.getIndex()))
                .anyMatch(group -> group.getMaxTargets() > 1 || group.getMinTargets() == 0
                        || group.getDynamicMinTargets() != null);
    }

    private int beginAttackTriggerCopies(GameData gameData, UUID controllerId, Permanent source) {
        return gameData.beginTriggeredAbilityCopies(1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, controllerId, source, true));
    }

    /**
     * For attached permanents (equipment/auras) with abilities that trigger per blocking creature
     * (e.g. Infiltration Lens: "Whenever equipped creature becomes blocked by a creature,
     * you may draw two cards"), creates one stack entry for each such ability and blocker, with all the ability's
     * effects (CR 603.3); two abilities are two objects (CR 603.3b). An ability with a filter on the blocker
     * ("becomes blocked by a Vampire", Wooden Stake) triggers for each blocker it matches as the blocker becomes a
     * blocking creature (CR 509.3d, 509.3f); its event condition and intervening "if" are read from the attacker
     * (CR 603.2, 603.4).
     */
    public void checkAttachedPerBlockerTriggers(GameData gameData, Permanent attacker,
                                                 List<BlockerAssignment> blockerAssignments,
                                                 List<Permanent> defenderBattlefield, int attackerIndex) {
        UUID controllerId = gameData.findControllerOf(attacker);
        if (controllerId == null) return;
        final UUID finalControllerId = controllerId;

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(attacker.getId())) {
                return;
            }
            List<List<CombatAbilityReader.CombatEffect>> perBlockerAbilities =
                    attachedCombatAbilities(perm, CombatWatch.ON_BECOMES_BLOCKED).stream()
                            .filter(ability -> ability.getFirst().eachBlocker())
                            .toList();
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.attackerIndex() != attackerIndex) {
                    continue;
                }
                Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
                for (List<CombatAbilityReader.CombatEffect> ability : perBlockerAbilities) {
                    CombatAbilityReader.CombatEffect read = ability.getFirst();
                    if ((read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, blocker, read.filter()))
                            || !conditionEvaluationService.triggerConditionsMet(gameData, read.options(),
                            ConditionContext.forPermanent(attacker, finalControllerId))) {
                        continue;
                    }
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            ownerId,
                            perm.getCard().getName() + "'s triggered ability",
                            CombatAbilityReader.effectsAsJudged(ability),
                            blocker.getId(),
                            perm.getId()
                    );
                    // "That creature" is the blocker, the entry's non-targeting target whatever the effects are.
                    trigger.setNonTargeting(true);
                    trigger.setTriggeringPermanentId(attacker.getId());
                    // "It" and "its controller" are the attacking creature, whatever the effects are.
                    trigger.setTriggeringPermanentControllerId(finalControllerId);
                    trigger.setAttachedPermanentSnapshot(new Permanent(attacker));
                    putOnStackOrChooseMode(gameData, trigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} per-blocker trigger pushed onto stack (attached to {})",
                            gameData.id, perm.getCard().getName(), attacker.getCard().getName());
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
