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
import com.github.laxika.magicalvibes.model.effect.CombatOpponentFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CombatOpponentReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySubtypeCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsDamageToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
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
     * The effects of the attached permanent {@code perm}'s abilities that {@code watch} reads: its own "whenever
     * equipped / enchanted creature attacks, blocks or becomes blocked", or the ability it grants the creature. No
     * Aura or Equipment writes one with a filter or a limit, and this throws on one.
     */
    private static List<CombatAbilityReader.CombatEffect> attachedCombatEffects(Permanent perm, CombatWatch watch) {
        List<CombatAbilityReader.CombatEffect> reads = CombatAbilityReader.read(
                WatchedAbilities.printed(perm.getCard(), watch), watch.trigger());
        for (CombatAbilityReader.CombatEffect read : reads) {
            if (read.filter() != null || read.limit() != TriggerLimit.NONE) {
                throw new IllegalStateException("The attached-permanent combat collector reads no filter or limit: "
                        + read.ability());
            }
        }
        return reads;
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
     * Overload that accepts a combat opponent for subtype-filtered triggers
     * (e.g. Wooden Stake's "blocks or becomes blocked by a Vampire").
     * When a {@link DestroySubtypeCombatOpponentEffect} matches the opponent's subtype,
     * the trigger is auto-targeted and placed directly on the stack as non-targeting.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, CombatWatch slot,
                                              Permanent combatOpponent) {
        checkAuraTriggersForCreature(gameData, creature, slot, combatOpponent, null);
    }

    /**
     * Checks attached triggers while allowing the block collector to suppress each attached
     * permanent's "blocks" once each combat ability after its first trigger in the block declaration.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, CombatWatch slot,
                                              Permanent combatOpponent,
                                              Set<UUID> oncePerBlockTriggeredPermanents) {
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
                List<CombatAbilityReader.CombatEffect> auraRegs = attachedCombatEffects(perm, slot);
                // Skip per-blocker effects ("becomes blocked by a creature", CR 509.3d) — they are handled by
                // checkAttachedPerBlockerTriggers. "Blocks" once each combat (CR 509.3a) triggers for the first
                // block of the declaration only.
                boolean hasOncePerBlockRegistration = auraRegs.stream()
                        .anyMatch(CombatAbilityReader.CombatEffect::oneOrMore);
                boolean includeOncePerBlockRegistration = !hasOncePerBlockRegistration
                        || oncePerBlockTriggeredPermanents == null
                        || oncePerBlockTriggeredPermanents.add(perm.getId());
                List<CombatAbilityReader.CombatEffect> queuedRegistrations = auraRegs.stream()
                        .filter(r -> !r.eachBlocker())
                        .filter(r -> !r.oneOrMore() || includeOncePerBlockRegistration)
                        .toList();
                // Each ability is one object on the stack (CR 603.3): Reaper's Talisman's two.
                // The intervening "if" (CR 603.4) and event condition (CR 603.2: "attacks alone", Sigil of Valor) are read
                // from the creature, against its attack target ("if it's the first combat phase", Genji Glove;
                // "if the defending player is the monarch").
                ConditionContext triggerContext = ConditionContext.forPermanent(creature, finalCreatureControllerId)
                        .withTargetId(slot == CombatWatch.ON_ATTACK ? creature.getAttackTarget() : null);
                for (List<CombatAbilityReader.CombatEffect> ability : CombatAbilityReader.byAbility(queuedRegistrations)) {
                    if (ability.isEmpty() || !conditionEvaluationService.triggerConditionsMet(gameData,
                            ability.getFirst().ability().options(), triggerContext)) {
                        continue;
                    }
                    List<CardEffect> nonPerBlockerEffects = CombatAbilityReader.effectsOf(ability);
                    // Bake the creature's controller into effects that need it,
                    // and transform subtype-filtered combat effects
                    List<CardEffect> effectsForStack = new ArrayList<>();
                    boolean autoTargetOpponent = false;
                    for (CardEffect effect : nonPerBlockerEffects) {
                        if (effect instanceof DestroySubtypeCombatOpponentEffect destroyEffect) {
                            if (combatOpponent != null
                                    && permanentHasSubtype(combatOpponent, destroyEffect.requiredSubtype())) {
                                effectsForStack.add(new DestroyTargetPermanentEffect(destroyEffect.cannotBeRegenerated()));
                                autoTargetOpponent = true;
                            }
                            // If subtype doesn't match, skip this effect
                        } else if (effect instanceof CombatOpponentFilterEffect conditional) {
                            if (matchesCombatOpponent(gameData, creature, combatOpponent,
                                    conditional.opponentFilter())) {
                                effectsForStack.add(conditional.wrapped());
                            }
                        } else if (slot != CombatWatch.ON_ATTACK
                                && effect instanceof CombatOpponentReferencingEffect c
                                && c.referencesCombatOpponent()) {
                            if (combatOpponent != null
                                    && (!(effect instanceof DestroyCombatOpponentAtEndOfCombatEffect destroyEffect)
                                    || predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, combatOpponent, destroyEffect.filter()))) {
                                effectsForStack.add(effect);
                                autoTargetOpponent = true;
                            }
                        } else if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                            effectsForStack.add(new EnchantedCreatureControllerLosesLifeEffect(e.amount(), finalCreatureControllerId));
                        } else {
                            effectsForStack.add(effect);
                        }
                    }
                    // An Equipment's or Aura's own "have target creature block it" (Grappling Hook) refers to
                    // the attacking creature, not to the attached permanent that is the trigger's source.
                    if (slot == CombatWatch.ON_ATTACK) {
                        effectsForStack.replaceAll(e -> e instanceof MayEffect may
                                && may.wrapped() instanceof MustBlockSourceEffect mustBlock
                                && mustBlock.sourcePermanentId() == null
                                ? new MayEffect(new MustBlockSourceEffect(creature.getId(),
                                        mustBlock.targetRestriction(), mustBlock.untilEndOfCombat()),
                                        may.prompt(), may.elseEffect(), may.choicePlayer(), may.choicePlayerId())
                                : e);
                    }
                    if (effectsForStack.isEmpty()) continue;

                    int previousCopies = slot == CombatWatch.ON_ATTACK
                            ? beginAttackTriggerCopies(gameData, auraOwnerId, perm)
                            : -1;
                    try {
                        if (autoTargetOpponent) {
                            // Auto-targeted combat trigger — goes directly on the stack
                            StackEntry trigger = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    auraOwnerId,
                                    perm.getCard().getName() + "'s triggered ability",
                                    effectsForStack,
                                    combatOpponent.getId(),
                                    perm.getId()
                            );
                            trigger.setNonTargeting(true);
                            trigger.setTriggeringPermanentId(creature.getId());
                            // Bake attacked player/planeswalker so DEFENDING_PLAYER effects
                            // (e.g. equipment-granted Afflict) can resolve.
                            trigger.setAttackedTargetId(creature.getAttackTarget());
                            trigger.setTriggeringPermanentId(creature.getId());
                            captureEquippedCreatureDamageSource(trigger, creature, finalCreatureControllerId, effectsForStack);
                            gameData.stack.add(trigger);
                            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                            log.info("Game {} - {} auto-targeted combat trigger pushed onto stack (attached to {})",
                                    gameData.id, perm.getCard().getName(), creature.getCard().getName());
                        } else if (slot == CombatWatch.ON_ATTACK && effectsForStack.stream()
                                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
                            UUID attackedTargetId = creature.getAttackTarget();
                            UUID defendingPlayerId = attackedTargetId == null ? null
                                    : gameData.playerIds.contains(attackedTargetId)
                                            ? attackedTargetId
                                            : gameQueryService.findPermanentController(gameData, attackedTargetId);
                            graveyardTargetingService.handleAttackGraveyardTargeting(
                                    gameData, auraOwnerId, perm.getCard(), effectsForStack, perm.getId(),
                                    defendingPlayerId);
                            continue;
                        } else {
                            // Check if any effect needs a permanent target — queue for target selection
                            boolean needsTarget = effectsForStack.stream()
                                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                            if (needsTarget) {
                                if (perm.getCard().isAura() && perm.getCard().getTargetGroups().size() > 1
                                        && effectsForStack.stream().noneMatch(effect ->
                                        perm.getCard().getEffectTargetIndex(effect) >= 0)) {
                                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                            perm.getCard(), auraOwnerId, effectsForStack, perm.getId(),
                                            List.of(), 1, 0, List.of(0), 0, List.of(), false, null,
                                            creature.getId()));
                                } else if (needsSlotBySlotTargetSelection(perm.getCard(), effectsForStack)) {
                                    int firstGroup = effectsForStack.stream()
                                            .mapToInt(perm.getCard()::getEffectTargetIndex)
                                            .filter(index -> index >= 0).min().orElse(0);
                                    List<Integer> skippedGroups = java.util.Collections.nCopies(firstGroup, 0);
                                    gameData.queueInteraction(
                                            new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                                    perm.getCard(), auraOwnerId, effectsForStack, perm.getId(),
                                                    List.of(), firstGroup, 0, skippedGroups, 0, List.of(), false,
                                                    null, creature.getId()));
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
                                    gameData.queueInteraction(
                                            slot == CombatWatch.ON_ATTACK
                                                    ? new PermanentChoiceContext.AttackTriggerTarget(
                                                            perm.getCard(), grantedAttackControllerId, effectsForStack,
                                                            permOwnedAttackTrigger ? perm.getId() : creature.getId(),
                                                            targetChooserId, attackedTargetId, creature.getId())
                                                    : new PermanentChoiceContext.AttackTriggerTarget(
                                                            perm.getCard(), auraOwnerId, effectsForStack, perm.getId(),
                                                            auraOwnerId, null, creature.getId()));
                                }
                                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                                log.info("Game {} - {} targeted attack trigger queued for target selection (attached to {})",
                                        gameData.id, perm.getCard().getName(), creature.getCard().getName());
                            } else {
                                UUID triggeringPlayerId = effectsForStack.stream()
                                        .anyMatch(CreateTokenForTriggeringPlayerEffect.class::isInstance)
                                        ? finalCreatureControllerId
                                        : null;
                                // An attack ability granted to the enchanted creature (Invocation of Saint Traft,
                                // Unquenchable Fury) is an ability of that creature, so the creature is its source
                                // and its controller controls it (CR 113.7, CR 113.8).
                                boolean grantedToCreature = slot == CombatWatch.ON_ATTACK
                                        && (triggeringPlayerId != null || !isOwnAttackTrigger(perm, slot));
                                StackEntry trigger = new StackEntry(
                                        StackEntryType.TRIGGERED_ABILITY,
                                        perm.getCard(),
                                        grantedToCreature ? finalCreatureControllerId : auraOwnerId,
                                        perm.getCard().getName() + "'s triggered ability",
                                        effectsForStack,
                                        triggeringPlayerId,
                                        grantedToCreature ? creature.getId() : perm.getId()
                                );
                                if (triggeringPlayerId != null) {
                                    trigger.setNonTargeting(true);
                                }
                                // "Its controller loses life" reads the enchanted creature's controller as the ability
                                // resolves, also as a step or behind an "if" (Scriv's Contract's "otherwise").
                                if (effectsForStack.stream().anyMatch(
                                        CombatTriggerService::losesLifeAsEnchantedCreatureController)) {
                                    trigger.setTargetId(creature.getId());
                                    trigger.setNonTargeting(true);
                                    trigger.setTriggeringPermanentControllerId(finalCreatureControllerId);
                                }
                                // Bake attacked player/planeswalker so DEFENDING_PLAYER effects
                                // (e.g. equipment-granted Afflict) can resolve.
                                trigger.setAttackedTargetId(creature.getAttackTarget());
                                trigger.setDefendingPlayerId(creature.getAttackTarget() == null ? null
                                        : gameData.playerIds.contains(creature.getAttackTarget())
                                                ? creature.getAttackTarget()
                                                : gameQueryService.findPermanentController(gameData, creature.getAttackTarget()));
                                trigger.setTriggeringPermanentId(creature.getId());
                                if (effectsForStack.stream().anyMatch(
                                        EquippedCreatureDealsDamageToDefendingPlayerEffect.class::isInstance)) {
                                    trigger.setTriggeringPermanentControllerId(finalCreatureControllerId);
                                }
                                trigger.setSourcePermanentSnapshot(new Permanent(grantedToCreature ? creature : perm));
                                trigger.setAttachedPermanentSnapshot(new Permanent(creature));
                                trigger.rememberLastKnownPermanentCard(creature.getId(), creature.getCard());
                                gameData.stack.add(trigger);
                                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                                log.info("Game {} - {} aura trigger pushed onto stack (enchanted creature {})",
                                        gameData.id, perm.getCard().getName(), creature.getCard().getName());
                            }
                        }
                    } finally {
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
     * For attached permanents (equipment/auras) with effects that trigger per blocking creature
     * (e.g. Infiltration Lens: "Whenever equipped creature becomes blocked by a creature,
     * you may draw two cards"), creates one stack entry per blocker.
     * <p>
     * For {@link DestroySubtypeCombatOpponentEffect} effects, the blocker's subtype is checked
     * and the effect is transformed to {@link DestroyTargetPermanentEffect} with auto-targeting
     * (e.g. Wooden Stake's "becomes blocked by a Vampire" trigger).
     */
    public void checkAttachedPerBlockerTriggers(GameData gameData, Permanent attacker,
                                                 List<BlockerAssignment> blockerAssignments,
                                                 List<Permanent> defenderBattlefield, int attackerIndex) {
        UUID controllerId = gameData.findControllerOf(attacker);
        if (controllerId == null) return;
        final UUID finalControllerId = controllerId;

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(attacker.getId())) {
                List<CardEffect> perBlockerEffects = attachedCombatEffects(perm, CombatWatch.ON_BECOMES_BLOCKED).stream()
                        .filter(CombatAbilityReader.CombatEffect::eachBlocker)
                        .map(CombatAbilityReader.CombatEffect::effect)
                        .toList();
                if (!perBlockerEffects.isEmpty()) {
                    for (BlockerAssignment assignment : blockerAssignments) {
                        if (assignment.attackerIndex() != attackerIndex) {
                            continue;
                        }
                        Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());

                        // Transform subtype-filtered effects for this specific blocker
                        List<CardEffect> transformedEffects = new ArrayList<>();
                        boolean autoTargetBlocker = false;
                        for (CardEffect effect : perBlockerEffects) {
                            if (effect instanceof DestroySubtypeCombatOpponentEffect destroyEffect) {
                                if (permanentHasSubtype(blocker, destroyEffect.requiredSubtype())) {
                                    transformedEffects.add(new DestroyTargetPermanentEffect(destroyEffect.cannotBeRegenerated()));
                                    autoTargetBlocker = true;
                                }
                                // If subtype doesn't match, skip this effect for this blocker
                            } else if (effect instanceof CombatOpponentFilterEffect conditional) {
                                if (predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, blocker, conditional.opponentFilter())) {
                                    transformedEffects.add(conditional.wrapped());
                                }
                            } else if (effect instanceof CombatOpponentReferencingEffect c && c.referencesCombatOpponent()) {
                                if (!(effect instanceof DestroyCombatOpponentAtEndOfCombatEffect destroyEffect)
                                        || predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, blocker, destroyEffect.filter())) {
                                    transformedEffects.add(effect);
                                    autoTargetBlocker = true;
                                }
                            } else {
                                transformedEffects.add(effect);
                            }
                        }
                        if (transformedEffects.isEmpty()) continue;

                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                ownerId,
                                perm.getCard().getName() + "'s triggered ability",
                                transformedEffects,
                                autoTargetBlocker ? blocker.getId() : null,
                                perm.getId()
                        );
                        if (autoTargetBlocker) {
                            trigger.setNonTargeting(true);
                        }
                        trigger.setTriggeringPermanentId(attacker.getId());
                        captureEquippedCreatureDamageSource(trigger, attacker, finalControllerId, transformedEffects);
                        gameData.stack.add(trigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} per-blocker trigger pushed onto stack (attached to {})",
                                gameData.id, perm.getCard().getName(), attacker.getCard().getName());
                    }
                }
            }
        });
    }

    private static void captureEquippedCreatureDamageSource(StackEntry trigger, Permanent creature,
                                                             UUID controllerId, List<CardEffect> effects) {
        if (effects.stream().anyMatch(EquippedCreatureDealsDamageToDefendingPlayerEffect.class::isInstance)) {
            trigger.setDamageSourceCard(creature.getCard());
            trigger.setTriggeringPermanentControllerId(controllerId);
        }
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

    /**
     * Checks whether a permanent has a given subtype. Checks base subtypes, transient subtypes,
     * granted subtypes, and the intrinsic Changeling keyword (which grants all creature subtypes).
     */
    private static boolean permanentHasSubtype(Permanent permanent, CardSubtype subtype) {
        return GameQueryService.permanentHasSubtype(permanent, subtype);
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

    /**
     * Whether {@code effect} has the enchanted creature's controller lose life, alone, as a step of a sequence or
     * behind an effect's "if".
     */
    private static boolean losesLifeAsEnchantedCreatureController(CardEffect effect) {
        return switch (effect) {
            case EnchantedCreatureControllerLosesLifeEffect ignored -> true;
            case ConditionalEffect conditional -> losesLifeAsEnchantedCreatureController(conditional.wrapped());
            case SequenceEffect sequence -> sequence.steps().stream()
                    .anyMatch(CombatTriggerService::losesLifeAsEnchantedCreatureController);
            default -> false;
        };
    }
}
