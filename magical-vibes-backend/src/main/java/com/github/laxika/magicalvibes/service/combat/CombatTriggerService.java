package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles combat trigger collection helpers shared across attack, block, and damage phases.
 * Provides aura/equipment trigger checking and APNAP trigger reordering.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatTriggerService {

    private final GameBroadcastService gameBroadcastService;

    /**
     * Checks attached permanents (auras/equipment) for triggers in the given slot
     * on the specified creature. Queues targeted triggers for target selection,
     * pushes non-targeted triggers directly onto the stack.
     */
    public void checkAuraTriggersForCreature(GameData gameData, Permanent creature, EffectSlot slot) {
        UUID creatureControllerId = CombatHelper.findControllerOf(gameData, creature);
        if (creatureControllerId == null) return;
        final UUID finalCreatureControllerId = creatureControllerId;

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())) {
                List<EffectRegistration> auraRegs = perm.getCard().getEffectRegistrations(slot);
                // Skip per-blocker effects — they are handled by checkAttachedPerBlockerTriggers
                List<CardEffect> nonPerBlockerEffects = auraRegs.stream()
                        .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();
                if (!nonPerBlockerEffects.isEmpty()) {
                    // Bake the creature's controller into effects that need it
                    List<CardEffect> effectsForStack = new ArrayList<>();
                    for (CardEffect effect : nonPerBlockerEffects) {
                        if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                            effectsForStack.add(new EnchantedCreatureControllerLosesLifeEffect(e.amount(), finalCreatureControllerId));
                        } else {
                            effectsForStack.add(effect);
                        }
                    }

                    // Check if any effect needs a permanent target — queue for target selection
                    boolean needsTarget = effectsForStack.stream().anyMatch(CardEffect::canTargetPermanent);
                    if (needsTarget) {
                        gameData.pendingAttackTriggerTargets.add(
                                new PermanentChoiceContext.AttackTriggerTarget(
                                        perm.getCard(), auraOwnerId, effectsForStack, perm.getId()));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} targeted attack trigger queued for target selection (attached to {})",
                                gameData.id, perm.getCard().getName(), creature.getCard().getName());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                auraOwnerId,
                                perm.getCard().getName() + "'s triggered ability",
                                effectsForStack,
                                null,
                                perm.getId()
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} aura trigger pushed onto stack (enchanted creature {})",
                                gameData.id, perm.getCard().getName(), creature.getCard().getName());
                    }
                }
            }
        });
    }

    /**
     * For attached permanents (equipment/auras) with effects that trigger per blocking creature
     * (e.g. Infiltration Lens: "Whenever equipped creature becomes blocked by a creature,
     * you may draw two cards"), creates one stack entry per blocker.
     */
    public void checkAttachedPerBlockerTriggers(GameData gameData, Permanent attacker,
                                                 List<BlockerAssignment> blockerAssignments,
                                                 List<Permanent> defenderBattlefield, int attackerIndex) {
        UUID controllerId = CombatHelper.findControllerOf(gameData, attacker);
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
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                finalControllerId,
                                perm.getCard().getName() + "'s triggered ability",
                                new ArrayList<>(perBlockerEffects),
                                null,
                                perm.getId()
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} per-blocker trigger pushed onto stack (attached to {})",
                                gameData.id, perm.getCard().getName(), attacker.getCard().getName());
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
}
