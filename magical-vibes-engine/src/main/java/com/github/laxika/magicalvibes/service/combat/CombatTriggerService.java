package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CombatOpponentReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySubtypeCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsDamageToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

    /**
     * Checks attached permanents (auras/equipment) for triggers in the given slot
     * on the specified creature. Queues targeted triggers for target selection,
     * pushes non-targeted triggers directly onto the stack.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, EffectSlot slot) {
        checkAuraTriggersForCreature(gameData, creature, slot, null);
    }

    /**
     * Overload that accepts a combat opponent for subtype-filtered triggers
     * (e.g. Wooden Stake's "blocks or becomes blocked by a Vampire").
     * When a {@link DestroySubtypeCombatOpponentEffect} matches the opponent's subtype,
     * the trigger is auto-targeted and placed directly on the stack as non-targeting.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, EffectSlot slot,
                                              Permanent combatOpponent) {
        checkAuraTriggersForCreature(gameData, creature, slot, combatOpponent, null);
    }

    /**
     * Checks attached triggers while allowing the block collector to suppress each attached
     * permanent's ONCE_PER_BLOCK registration after its first trigger in the block declaration.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, EffectSlot slot,
                                              Permanent combatOpponent,
                                              Set<UUID> oncePerBlockTriggeredPermanents) {
        UUID creatureControllerId = gameData.findControllerOf(creature);
        if (creatureControllerId == null) return;
        final UUID finalCreatureControllerId = creatureControllerId;

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())) {
                List<EffectRegistration> auraRegs = perm.getCard().getEffectRegistrations(slot);
                // Skip per-blocker effects — they are handled by checkAttachedPerBlockerTriggers
                boolean hasOncePerBlockRegistration = auraRegs.stream()
                        .anyMatch(registration -> registration.triggerMode() == TriggerMode.ONCE_PER_BLOCK);
                boolean includeOncePerBlockRegistration = !hasOncePerBlockRegistration
                        || oncePerBlockTriggeredPermanents == null
                        || oncePerBlockTriggeredPermanents.add(perm.getId());
                List<CardEffect> nonPerBlockerEffects = auraRegs.stream()
                        .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                        .filter(r -> r.triggerMode() != TriggerMode.ONCE_PER_BLOCK
                                || includeOncePerBlockRegistration)
                        .map(EffectRegistration::effect)
                        .toList();
                if (!nonPerBlockerEffects.isEmpty()) {
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
                        } else if (effect instanceof TriggeringPermanentConditionalEffect conditional
                                && conditional.combatOpponent()) {
                            if (combatOpponent != null
                                    && predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, combatOpponent, conditional.predicate())) {
                                effectsForStack.add(conditional.wrapped());
                                autoTargetOpponent = true;
                            }
                        } else if (effect instanceof CombatOpponentReferencingEffect c && c.referencesCombatOpponent()) {
                            // "blocks or becomes blocked by a [filter] creature, ... that creature"
                            // (e.g. Venom). Auto-target the combat opponent; the effect's handler
                            // re-checks the filter at resolution.
                            if (combatOpponent != null) {
                                effectsForStack.add(effect);
                                autoTargetOpponent = true;
                            }
                        } else if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                            effectsForStack.add(new EnchantedCreatureControllerLosesLifeEffect(e.amount(), finalCreatureControllerId));
                        } else {
                            effectsForStack.add(effect);
                        }
                    }
                    // "Whenever equipped creature attacks alone" (Sigil of Valor): the granted
                    // ability doesn't trigger at all unless it's the only attacking creature its
                    // controller controls (CR 506.5), so drop it here instead of putting a
                    // do-nothing entry on the stack. Surviving conditionals are already satisfied
                    // and get unwrapped.
                    effectsForStack.removeIf(e -> e instanceof ConditionalEffect ce
                            && ce.condition() instanceof AttacksAlone
                            && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                    ConditionContext.forPermanent(creature, finalCreatureControllerId)));
                    effectsForStack.replaceAll(e -> e instanceof ConditionalEffect ce
                            && ce.condition() instanceof AttacksAlone ? ce.wrapped() : e);

                    if (effectsForStack.isEmpty()) return;

                    int previousCopies = slot == EffectSlot.ON_ATTACK
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
                        } else {
                            // Check if any effect needs a permanent target — queue for target selection
                            boolean needsTarget = effectsForStack.stream()
                                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                            if (needsTarget) {
                                gameData.queueInteraction(
                                        new PermanentChoiceContext.AttackTriggerTarget(
                                                perm.getCard(), auraOwnerId, effectsForStack, perm.getId()));
                                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                                log.info("Game {} - {} targeted attack trigger queued for target selection (attached to {})",
                                        gameData.id, perm.getCard().getName(), creature.getCard().getName());
                            } else {
                                UUID triggeringPlayerId = effectsForStack.stream()
                                        .anyMatch(CreateTokenForTriggeringPlayerEffect.class::isInstance)
                                        ? finalCreatureControllerId
                                        : null;
                                StackEntry trigger = new StackEntry(
                                        StackEntryType.TRIGGERED_ABILITY,
                                        perm.getCard(),
                                        auraOwnerId,
                                        perm.getCard().getName() + "'s triggered ability",
                                        effectsForStack,
                                        triggeringPlayerId,
                                        perm.getId()
                                );
                                if (triggeringPlayerId != null) {
                                    trigger.setNonTargeting(true);
                                }
                                // Bake attacked player/planeswalker so DEFENDING_PLAYER effects
                                // (e.g. equipment-granted Afflict) can resolve.
                                trigger.setAttackedTargetId(creature.getAttackTarget());
                                trigger.setTriggeringPermanentId(creature.getId());
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
                List<CardEffect> perBlockerEffects = perm.getCard().getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED).stream()
                        .filter(r -> r.triggerMode() == TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
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
                            } else if (effect instanceof TriggeringPermanentConditionalEffect conditional
                                    && conditional.combatOpponent()) {
                                if (predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, blocker, conditional.predicate())) {
                                    transformedEffects.add(conditional.wrapped());
                                    autoTargetBlocker = true;
                                }
                            } else if (effect instanceof CombatOpponentReferencingEffect c && c.referencesCombatOpponent()) {
                                // Auto-target this blocker; the handler re-checks the filter (Venom).
                                transformedEffects.add(effect);
                                autoTargetBlocker = true;
                            } else {
                                transformedEffects.add(effect);
                            }
                        }
                        if (transformedEffects.isEmpty()) continue;

                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                finalControllerId,
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
}
