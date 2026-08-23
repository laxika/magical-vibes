package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChandraTorchExileCastSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDividedDamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PsychicBattleSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles permanent choice contexts related to spell casting from non-hand zones
 * and spell retargeting.
 *
 * <p>Covers retargeting spells on the stack, and casting spells from the library,
 * exile, or graveyard that require a permanent/player target.
 */
@Slf4j
@Service
public class PermanentChoiceSpellHandlerService {

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    // @Lazy breaks the cycle: PermanentChoiceSpellHandlerService → ExileFreeCastQueueSupport →
    // PlayerInputService → InteractionHandlerRegistry → ImprovisationCapstoneCastChoiceInteractionHandler
    // → ExileFreeCastQueueSupport.
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final InputCompletionService inputCompletionService;
    private final PsychicBattleSupport psychicBattleSupport;
    private final SpellCastingService spellCastingService;
    private final ChandraTorchExileCastSupport chandraTorchExileCastSupport;
    private final TargetLegalityService targetLegalityService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CopySupport copySupport;
    private final LifeSupport lifeSupport;
    private final DealDividedDamageSupport dealDividedDamageSupport;

    public PermanentChoiceSpellHandlerService(GameQueryService gameQueryService,
                                              GraveyardService graveyardService,
                                              GameLogService gameLogService,
                                              TriggerCollectionService triggerCollectionService,
                                              PlayerInputService playerInputService,
                                              @Lazy ExileFreeCastQueueSupport exileFreeCastQueueSupport,
                                              ExileCastTargetSupport exileCastTargetSupport,
                                              @Lazy InputCompletionService inputCompletionService,
                                              PsychicBattleSupport psychicBattleSupport,
                                              @Lazy SpellCastingService spellCastingService,
                                              @Lazy ChandraTorchExileCastSupport chandraTorchExileCastSupport,
                                              TargetLegalityService targetLegalityService,
                                              InteractionHandlerRegistry interactionHandlerRegistry,
                                              CopySupport copySupport,
                                              LifeSupport lifeSupport,
                                              DealDividedDamageSupport dealDividedDamageSupport) {
        this.gameQueryService = gameQueryService;
        this.graveyardService = graveyardService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.playerInputService = playerInputService;
        this.exileFreeCastQueueSupport = exileFreeCastQueueSupport;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.inputCompletionService = inputCompletionService;
        this.psychicBattleSupport = psychicBattleSupport;
        this.spellCastingService = spellCastingService;
        this.chandraTorchExileCastSupport = chandraTorchExileCastSupport;
        this.targetLegalityService = targetLegalityService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.copySupport = copySupport;
        this.lifeSupport = lifeSupport;
        this.dealDividedDamageSupport = dealDividedDamageSupport;
    }

    public void handleSpellRetarget(GameData gameData, UUID permanentId, PermanentChoiceContext.SpellRetarget retarget) {
        StackEntry targetSpell = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(retarget.spellCardId())) {
                targetSpell = se;
                break;
            }
        }
        if (targetSpell == null) {
            log.info("Game {} - Target spell no longer on stack for retarget", gameData.id);
        } else {
            targetSpell.setTargetId(permanentId);
            String spellName = targetSpell.isCopy()
                    ? "Copy of " + targetSpell.getCard().getName()
                    : targetSpell.getCard().getName();
            String targetName = getTargetDisplayName(gameData, permanentId);
            String logMsg = spellName + " now targets " + targetName + ".";
            gameLogService.append(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} retargeted to {}", gameData.id, spellName, targetName);

            // Check becomes-target-of-spell triggers for the new target (e.g. Livewire Lash)
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, targetSpell);
            if (gameData.interaction.isAwaitingInput()) return;
        }

        // Resume any remaining effects on the retargeting spell/ability that were paused for this
        // async retarget (e.g. Wild Ricochet's "Then copy that spell" after retargeting the original).
        // For flows with nothing left to resolve this is a no-op that falls through to auto-pass.
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePsychicBattleRetarget(GameData gameData, UUID permanentId,
                                             PermanentChoiceContext.PsychicBattleRetarget retarget) {
        StackEntry targetSpell = psychicBattleSupport.findTargetEntry(gameData, retarget.spellCardId());
        if (targetSpell == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        psychicBattleSupport.replaceTarget(targetSpell, retarget.targetIndex(), permanentId);
        gameLogService.append(gameData, GameLog.text(
                targetSpell.getCard().getName() + " now targets " + getTargetDisplayName(gameData, permanentId) + "."));

        boolean nextChoiceQueued = psychicBattleSupport.queueNextChoice(
                gameData, retarget.sourceCard(), retarget.controllerId(),
                retarget.spellCardId(), retarget.targetIndex() + 1);

        if (targetSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData, targetSpell);
        } else {
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, targetSpell);
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        if (nextChoiceQueued) {
            playerInputService.processNextMayAbility(gameData);
        } else {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleLibraryCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.LibraryCastSpellTarget lct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);
        boolean isSpellTarget = isValidSpellTarget(gameData, lct.cardToCast(), lct.spellEffects(), permanentId,
                lct.controllerId(), 0);

        if (target != null || isPlayerTarget || isSpellTarget) {
            StackEntry entry = new StackEntry(
                    lct.spellType(),
                    lct.cardToCast(),
                    lct.controllerId(),
                    lct.cardToCast().getName(),
                    new ArrayList<>(lct.spellEffects()),
                    0,
                    permanentId,
                    null,
                    null,
                    isSpellTarget ? Zone.STACK : null,
                    null,
                    null
            );
            gameData.stack.add(entry);

            gameData.recordSpellCast(lct.controllerId(), lct.cardToCast());
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : isSpellTarget ? getTargetDisplayName(gameData, permanentId) : target.getCard().getName();
            
            gameLogService.append(gameData, GameLog.builder().card(lct.cardToCast()).text(" targets " + targetName + ".").build());
            log.info("Game {} - {} cast-from-library targets {}", gameData.id, lct.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, lct.cardToCast(), lct.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
            if (lct.cardsToBottom() != null) {
                beginBottomReorder(gameData, lct.controllerId(), lct.cardsToBottom());
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }
        } else if (lct.cardsToBottom() != null) {
            List<Card> uncastCards = new ArrayList<>(lct.cardsToBottom());
            uncastCards.add(lct.cardToCast());
            beginBottomReorder(gameData, lct.controllerId(), uncastCards);
            log.info("Game {} - {} Ripple cast target no longer exists", gameData.id, lct.cardToCast().getName());
        } else {
            graveyardService.addCardToGraveyard(gameData, lct.controllerId(), lct.cardToCast());
            gameLogService.append(gameData, GameLog.cardThen(lct.cardToCast(), "'s target is no longer valid. It is put into the graveyard."));
            log.info("Game {} - {} cast-from-library target no longer exists", gameData.id, lct.cardToCast().getName());
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private void beginBottomReorder(GameData gameData, UUID ownerId, List<Card> cards) {
        if (cards.isEmpty()) {
            return;
        }
        if (cards.size() == 1) {
            gameData.playerDecks.get(ownerId).add(cards.getFirst());
            return;
        }
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibraryReorder(
                ownerId,
                new ArrayList<>(cards),
                true,
                ownerId,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
    }

    public void handleExileCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.ExileCastSpellTarget ect) {
        // Multi-target spells (e.g. Echocasting Symposium: target player + target creature you control)
        // collect their targets one slot at a time, in the card's declared order.
        if (ect.cardToCast().getMaxTargets() > 1) {
            handleMultiTargetExileCast(gameData, permanentId, ect);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);
        boolean isSpellTarget = isValidSpellTarget(gameData, ect.cardToCast(), ect.spellEffects(), permanentId,
                ect.controllerId(), 0);
        // A cipher copy of a graveyard-targeting spell (Midnight Recovery) targets a card in a
        // graveyard, which is neither a permanent nor a player.
        boolean isGraveyardTarget = gameQueryService.findCardInGraveyardById(gameData, permanentId) != null;

        if (target != null || isPlayerTarget || isGraveyardTarget || isSpellTarget) {
            if (ect.resolutionCast()) {
                try {
                    spellCastingService.playCardFromExileAsResolutionCast(gameData,
                            new Player(ect.controllerId(), gameData.playerIdToName.get(ect.controllerId())),
                            ect.cardToCast().getId(), 0, permanentId, ect.copy());
                    if (ect.lifeLossAfterCast() > 0) {
                        lifeSupport.applyLifeLoss(gameData, ect.controllerId(), ect.lifeLossAfterCast(),
                                ect.cardToCast().getName());
                    }
                } catch (IllegalStateException ex) {
                    gameData.removeFromExile(ect.cardToCast().getId());
                    gameLogService.append(gameData, GameLog.cardThen(ect.cardToCast(),
                            " can't be cast and ceases to exist."));
                }
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (ect.genericCostReduction() > 0) {
                try {
                    spellCastingService.playCardFromExileAsResolutionCast(gameData,
                            new Player(ect.controllerId(), gameData.playerIdToName.get(ect.controllerId())),
                            ect.cardToCast().getId(), 0, permanentId);
                } catch (IllegalStateException ex) {
                    gameData.exilePlayCostModifiers.remove(ect.cardToCast().getId());
                    log.info("Game {} - reduced-cost exile cast of {} could not be completed",
                            gameData.id, ect.cardToCast().getName());
                }
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            StackEntry entry = isGraveyardTarget
                    ? new StackEntry(
                            ect.spellType(),
                            ect.cardToCast(),
                            ect.controllerId(),
                            ect.cardToCast().getName(),
                            new ArrayList<>(ect.spellEffects()),
                            permanentId,
                            Zone.GRAVEYARD)
                    : new StackEntry(
                            ect.spellType(),
                            ect.cardToCast(),
                            ect.controllerId(),
                            ect.cardToCast().getName(),
                            new ArrayList<>(ect.spellEffects()),
                            0,
                            permanentId,
                            null,
                            null,
                            isSpellTarget ? Zone.STACK : null,
                            null,
                            null
            );
            entry.setCopy(ect.copy());
            entry.setSourceZone(Zone.EXILE);
            if (gameData.spellsGrantedHasteOnEntry.remove(ect.cardToCast().getId())) {
                entry.getGrantedKeywordsOnEntry().add(Keyword.HASTE);
            }
            gameData.stack.add(entry);

            gameData.recordSpellCast(ect.controllerId(), ect.cardToCast());
            gameData.priorityPassedBy.clear();

            String targetName = getTargetDisplayName(gameData, permanentId);
            gameLogService.append(gameData, GameLog.builder().card(ect.cardToCast()).text(" targets " + targetName + " (Knowledge Pool).").build());
            log.info("Game {} - {} cast-from-exile targets {}", gameData.id, ect.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, ect.cardToCast(), ect.controllerId(), Zone.EXILE);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            gameData.spellsGrantedHasteOnEntry.remove(ect.cardToCast().getId());
            if (ect.genericCostReduction() > 0) {
                gameData.exilePlayCostModifiers.remove(ect.cardToCast().getId());
                gameLogService.append(gameData, GameLog.cardThen(ect.cardToCast(),
                        "'s target is no longer valid and it stays exiled."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            graveyardService.addCardToGraveyard(gameData, ect.controllerId(), ect.cardToCast());
            gameLogService.append(gameData, GameLog.cardThen(ect.cardToCast(), "'s target is no longer valid. It is put into the graveyard."));
            log.info("Game {} - {} cast-from-exile target no longer exists", gameData.id, ect.cardToCast().getName());
        }

        resumeAfterExileCast(gameData, ect.controllerId());
    }

    public void handleChandraTorchCastSpellTarget(GameData gameData, UUID permanentId,
                                                   PermanentChoiceContext.ChandraTorchCastSpellTarget context) {
        chandraTorchExileCastSupport.completeTarget(gameData, permanentId, context);
    }

    /**
     * Collects the targets of a multi-target spell cast from exile one slot at a time. Each response
     * fills the next declared target slot; while slots remain, computes the legal candidates for the
     * following slot and prompts again. Once every slot is filled the spell is put on the stack with
     * its ordered target list.
     */
    private void handleMultiTargetExileCast(GameData gameData, UUID permanentId,
                                            PermanentChoiceContext.ExileCastSpellTarget ect) {
        Card card = ect.cardToCast();
        List<UUID> chosen = new ArrayList<>(ect.chosenTargets());
        chosen.add(permanentId);

        int totalSlots = card.getMaxTargets();
        if (chosen.size() < totalSlots) {
            List<UUID> nextCandidates = exileCastTargetSupport.nextSlotCandidates(gameData, card, ect.controllerId(), chosen);
            if (nextCandidates.isEmpty()) {
                // The full target set was pre-validated before prompting, so this only happens if a
                // remaining slot's targets vanished mid-selection. The spell can't be legally cast:
                // a copy ceases to exist (CR 707.10a), a real card goes to its owner's graveyard.
                gameData.spellsGrantedHasteOnEntry.remove(card.getId());
                if (ect.genericCostReduction() > 0) {
                    gameData.exilePlayCostModifiers.remove(card.getId());
                } else if (!ect.copy()) {
                    graveyardService.addCardToGraveyard(gameData, ect.controllerId(), card);
                }
                gameLogService.append(gameData, GameLog.cardThen(card, "'s targets are no longer valid."));
                log.info("Game {} - {} multi-target cast-from-exile has no legal target for a remaining slot",
                        gameData.id, card.getName());
                resumeAfterExileCast(gameData, ect.controllerId());
                return;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    card, ect.controllerId(), ect.spellEffects(), ect.spellType(), ect.copy(), chosen,
                    ect.genericCostReduction()));
            playerInputService.beginPermanentChoice(gameData, ect.controllerId(), nextCandidates,
                    "Choose a target for " + card.getName() + ".");
            gameLogService.append(gameData, GameLog.builder().card(card).text(" targets " + getTargetDisplayName(gameData, permanentId) + " — choosing next target.").build());
            return;
        }

        // Every target slot is filled — put the spell on the stack preserving the declared order.
        if (ect.genericCostReduction() > 0) {
            try {
                spellCastingService.playCardFromExileAsResolutionCast(gameData,
                        new Player(ect.controllerId(), gameData.playerIdToName.get(ect.controllerId())),
                        card.getId(), 0, chosen);
            } catch (IllegalStateException ex) {
                gameData.exilePlayCostModifiers.remove(card.getId());
                log.info("Game {} - reduced-cost multi-target exile cast of {} could not be completed",
                        gameData.id, card.getName());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry entry = new StackEntry(
                ect.spellType(),
                card,
                ect.controllerId(),
                card.getName(),
                new ArrayList<>(ect.spellEffects()),
                0,
                chosen
        );
        entry.setCopy(ect.copy());
        entry.setSourceZone(Zone.EXILE);
        if (gameData.spellsGrantedHasteOnEntry.remove(card.getId())) {
            entry.getGrantedKeywordsOnEntry().add(Keyword.HASTE);
        }
        gameData.stack.add(entry);

        gameData.recordSpellCast(ect.controllerId(), card);
        gameData.priorityPassedBy.clear();

        List<String> targetNames = chosen.stream().map(id -> getTargetDisplayName(gameData, id)).toList();
        
        gameLogService.append(gameData, GameLog.builder().card(card).text(" targets " + String.join(", ", targetNames) + ".").build());
        log.info("Game {} - {} multi-target cast-from-exile targets {}", gameData.id, card.getName(), targetNames);

        triggerCollectionService.checkSpellCastTriggers(gameData, card, ect.controllerId(), Zone.EXILE);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);

        resumeAfterExileCast(gameData, ect.controllerId());
    }

    /**
     * Resumes turn flow after a spell cast from exile has been placed on the stack (or fizzled).
     * Improvisation Capstone casts a batch of exiled spells; a targeted one pauses for target
     * selection, so resume casting the remainder of the queue before yielding priority.
     */
    private void resumeAfterExileCast(GameData gameData, UUID controllerId) {
        exileFreeCastQueueSupport.castNextFromQueue(gameData, controllerId);
    }

    public void handleGraveyardCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.GraveyardCastSpellTarget gct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

        if (target != null || isPlayerTarget) {
            if (!gct.withoutPayingManaCost()) {
                try {
                    spellCastingService.paySpellManaCostFromNonHandZone(gameData, gct.controllerId(), gct.cardToCast(), 0,
                            Zone.GRAVEYARD);
                } catch (IllegalStateException ex) {
                    graveyardService.addCardToGraveyard(gameData, gct.controllerId(), gct.cardToCast());
                    gameLogService.append(gameData, GameLog.cardThen(gct.cardToCast(), " can't be cast because its mana cost can't be paid."));
                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                    return;
                }
            }
            StackEntry entry = new StackEntry(
                    gct.spellType(),
                    gct.cardToCast(),
                    gct.controllerId(),
                    gct.cardToCast().getName(),
                    new ArrayList<>(gct.spellEffects()),
                    0,
                    permanentId,
                    null
            );
            entry.setExileInsteadOfGraveyard(gct.exileInsteadOfGraveyard());
            entry.setOwnerIdOverride(gct.ownerId());
            entry.setSourceZone(Zone.GRAVEYARD);
            gameData.stack.add(entry);

            for (int i = 0; i < gct.copyCount(); i++) {
                Card copyCard = copySupport.createCopyCard(gct.cardToCast());
                StackEntry copyEntry = copySupport.createCopyStackEntry(
                        entry, copyCard, gct.controllerId(), entry.getTargetId());
                gameData.stack.add(copyEntry);
                copySupport.checkSpellCopyTriggers(gameData, copyEntry);
                if (copyEntry.getTargetId() != null) {
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            gct.cardToCast(), gct.controllerId(), List.of(new CopySpellEffect()),
                            "Choose new targets for the copy of " + gct.cardToCast().getName() + "?",
                            copyCard.getId()));
                }
            }

            gameData.recordSpellCast(gct.controllerId(), gct.cardToCast());
            if (gct.restrictAdditionalSpellsThisTurn()) {
                gameData.preventAdditionalSpellCastsThisTurn(gct.controllerId());
            }
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : target.getCard().getName();
            
            gameLogService.append(gameData, GameLog.builder().card(gct.cardToCast()).text(" targets " + targetName + ".").build());
            log.info("Game {} - {} cast-from-graveyard targets {}", gameData.id, gct.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, gct.cardToCast(), gct.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            UUID ownerId = gct.ownerId() != null ? gct.ownerId() : gct.controllerId();
            graveyardService.addCardToGraveyard(gameData, ownerId, gct.cardToCast());
            gameLogService.append(gameData, GameLog.cardThen(gct.cardToCast(), "'s target is no longer valid. It is put into the graveyard."));
            log.info("Game {} - {} cast-from-graveyard target no longer exists", gameData.id, gct.cardToCast().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleHandCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.HandCastSpellTarget hct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        Card graveyardTarget = gameQueryService.findCardInGraveyardById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);
        boolean isPermanentOrPlayerTarget = (target != null || isPlayerTarget)
                && targetLegalityService.checkSpellTargeting(
                        gameData, hct.cardToCast(), permanentId, null, hct.controllerId(),
                        EffectResolution.needsTarget(hct.cardToCast()), hct.xValue(), false,
                        hct.castForMadnessCost())
                .isEmpty();
        boolean isGraveyardTarget = graveyardTarget != null
                && hct.spellEffects().stream()
                .anyMatch(effect -> effect.targetSpec().admits(
                        com.github.laxika.magicalvibes.model.effect.TargetPredicate.Kind.GRAVEYARD_CARD));
        if (isGraveyardTarget) {
            try {
                targetLegalityService.validateGraveyardEffectTargetOnly(
                        gameData, hct.cardToCast(), hct.spellEffects(), permanentId, hct.xValue());
            } catch (IllegalStateException e) {
                isGraveyardTarget = false;
            }
        }
        boolean isSpellTarget = isValidSpellTarget(gameData, hct.cardToCast(), hct.spellEffects(), permanentId,
                hct.controllerId(), hct.xValue());

        if (isPermanentOrPlayerTarget || isGraveyardTarget || isSpellTarget) {
            Map<UUID, Integer> damageAssignments = hct.castForMadnessCost()
                    && EffectResolution.needsDamageDistribution(hct.spellEffects())
                    ? Map.of(permanentId, dealDividedDamageSupport.damageAssignedToSingleTarget(
                            gameData, hct.spellEffects(), hct.controllerId(), hct.xValue()))
                    : null;
            StackEntry entry = new StackEntry(
                    hct.spellType(),
                    hct.cardToCast(),
                    hct.controllerId(),
                    hct.cardToCast().getName(),
                    new ArrayList<>(hct.spellEffects()),
                    hct.xValue(),
                    permanentId,
                    null,
                    damageAssignments,
                    isGraveyardTarget ? Zone.GRAVEYARD : isSpellTarget ? Zone.STACK : null,
                    null,
                    null
            );
            entry.setMadness(hct.castForMadnessCost());
            gameData.stack.add(entry);

            gameData.recordSpellCast(hct.controllerId(), hct.cardToCast());
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : isGraveyardTarget ? graveyardTarget.getName()
                    : isSpellTarget ? getTargetDisplayName(gameData, permanentId) : target.getCard().getName();
            gameLogService.append(gameData, GameLog.builder().card(hct.cardToCast()).text(" targets " + targetName + ".").build());
            log.info("Game {} - {} cast-from-hand targets {}", gameData.id, hct.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, hct.cardToCast(), hct.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            graveyardService.addCardToGraveyard(gameData, hct.controllerId(), hct.cardToCast());
            gameLogService.append(gameData, GameLog.cardThen(hct.cardToCast(), "'s target is no longer valid. It is put into the graveyard."));
            log.info("Game {} - {} cast-from-hand target no longer exists", gameData.id, hct.cardToCast().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleOpponentChosenSpellTarget(GameData gameData, UUID chosenId,
                                                 PermanentChoiceContext.OpponentChosenSpellTarget context) {
        if (context.chosenOpponentId() == null) {
            List<UUID> validTargets = targetLegalityService.computeValidOpponentChosenTargetPermanents(
                    gameData, context.cardToCast(), context.caster().getId(), chosenId,
                    context.xValue() != null ? context.xValue() : 0, false);
            if (validTargets.isEmpty()) {
                throw new IllegalStateException("No legal creature target remains");
            }
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.OpponentChosenSpellTarget(
                            context.caster(), context.cardToCast(), context.cardIndex(), context.xValue(),
                            context.buyback(), chosenId));
            playerInputService.beginPermanentChoice(gameData, chosenId, validTargets,
                    "Choose a creature for " + context.cardToCast().getName() + ".");
            return;
        }

        if (!context.chosenOpponentId().equals(gameQueryService.findPermanentController(gameData, chosenId))
                || !targetLegalityService.checkSpellTargeting(
                gameData, context.cardToCast(), chosenId, null, context.caster().getId(),
                EffectResolution.needsTarget(context.cardToCast()), context.xValue() != null ? context.xValue() : 0,
                false, false).isEmpty()) {
            throw new IllegalStateException("Invalid creature target");
        }

        spellCastingService.playCardAfterOpponentChosenTarget(
                gameData, context.caster(), context.cardIndex(), context.xValue(), chosenId,
                context.chosenOpponentId(), context.buyback());
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private boolean isValidSpellTarget(GameData gameData, Card card, List<CardEffect> spellEffects,
                                       UUID targetId, UUID controllerId, int xValue) {
        return EffectResolution.needsSpellTarget(spellEffects)
                && targetLegalityService.checkSpellTargetOnStack(
                        gameData, targetId, card.getTargetFilter(), controllerId, null, xValue).isEmpty();
    }

    private String getTargetDisplayName(GameData gameData, UUID targetId) {
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) return playerName;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetId)) return se.getCard().getName();
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(targetId)) return p.getCard().getName();
            }
        }

        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        if (graveyardCard != null) return graveyardCard.getName();

        return targetId.toString();
    }
}
