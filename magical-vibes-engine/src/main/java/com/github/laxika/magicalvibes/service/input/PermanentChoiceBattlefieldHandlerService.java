package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CipherSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PopulateSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SoulbondSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokensAndAttachEquipmentSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeCreatureThenMassDamageEqualToPowerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeOtherCreatureThenRevealUntilLowerManaValueEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificePermanentAndReturnTargetCardsFromGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMaySacrificeLandPutSourceOnTopEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachTargetAuraToAnotherPermanentOfSameTypeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TransformChosenPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCounterOnEitherTargetPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.OpponentChoosesPermanentToSacrificeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MayReturnPermanentToHandAndEnterWithCountersEffectHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles permanent choice contexts related to battlefield manipulation.
 *
 * <p>Covers clone copies, aura grafting, legend rule, sacrificing creatures,
 * activated ability cost choices, bouncing, damage prevention source choices,
 * sacrifice-for-divided-damage, and aura ETB placement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceBattlefieldHandlerService {
    private final CreateTokensAndAttachEquipmentSupport createTokensAndAttachEquipmentSupport;

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CloneService cloneService;
    private final TurnFaceUpCopyService turnFaceUpCopyService;
    private final CipherSupport cipherSupport;
    private final WarpWorldService warpWorldService;
    private final GameLogService gameLogService;
    private final AuraCopyService auraCopyService;
    private final AbilityActivationService abilityActivationService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateBasedActionService stateBasedActionService;
    private final TriggerCollectionService triggerCollectionService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final CreatureControlService creatureControlService;
    private final PopulateSupport populateSupport;
    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport exileSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.GargantuanGorillaUpkeepSupport gargantuanGorillaUpkeepSupport;
    private final LifeSupport lifeSupport;
    private final com.github.laxika.magicalvibes.service.DrawService drawService;
    private final LibrarySearchSupport librarySearchSupport;
    private final SoulbondSupport soulbondSupport;
    private final CoinFlipService coinFlipService;
    private final MayAbilityTapCostService mayAbilityTapCostService;
    private final TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler sacrificeCreatureCreateTokensIfSubtypeHandler;
    private final SacrificeCreatureThenMassDamageEqualToPowerEffectHandler sacrificeCreatureThenMassDamageHandler;
    private final SacrificeOtherCreatureThenRevealUntilLowerManaValueEffectHandler sacrificeOtherCreatureThenRevealHandler;
    private final SacrificePermanentAndReturnTargetCardsFromGraveyardEffectHandler sacrificePermanentAndReturnHandler;
    private final AnyPlayerMaySacrificeLandPutSourceOnTopEffectHandler anyPlayerMaySacrificeLandHandler;
    private final SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffectHandler patternMatcherHandler;
    private final AttachTargetAuraToAnotherPermanentOfSameTypeEffectHandler attachTargetAuraHandler;
    private final PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffectHandler guardDogsHandler;
    private final TransformChosenPermanentEffectHandler transformChosenPermanentEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.TariffSupport tariffSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.JuxtaposeSupport juxtaposeSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;
    private final PutCounterOnEitherTargetPermanentEffectHandler putCounterOnEitherTargetEffectHandler;
    private final MayReturnPermanentToHandAndEnterWithCountersEffectHandler mayReturnPermanentToHandAndEnterWithCountersEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.BlightEffectHandler blightEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentBlightsEffectHandler eachOpponentBlightsEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachTargetPlayerLosesLifeAndSacrificesCreatureEffectHandler eachTargetPlayerLosesLifeAndSacrificesCreatureEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentChoosesCreatureYouGainControlEffectHandler eachOpponentChoosesCreatureYouGainControlEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ChooseOpponentGainsControlOfSourceEffectHandler chooseOpponentGainsControlOfSourceEffectHandler;
    private final OpponentChoosesPermanentToSacrificeEffectHandler opponentChoosesPermanentToSacrificeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffectHandler anyOpponentSacrificeForTapAndCounterHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.OpponentChoosesCreatureTheyControlTokenCopyEffectHandler opponentChoosesCreatureTheyControlTokenCopyEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokenCopyOfChosenPermanentYouControlEffectHandler createTokenCopyOfChosenPermanentYouControlEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DefendingPlayerChoosesCreatureToBlockEffectHandler defendingPlayerChoosesCreatureToBlockEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.BalduvianWarlordEffectHandler balduvianWarlordEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffectHandler makeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffectHandler;

    /**
     * Retribution: the creatures' controller has picked which of the two targets to sacrifice; the
     * other target gets a -1/-1 counter. The counter is placed even if the sacrifice triggered
     * something, mirroring the single resolution step the card describes.
     */
    public void handleSacrificeOneOfTwoThenCounterOnOther(GameData gameData, UUID permanentId,
                                                          PermanentChoiceContext.SacrificeOneOfTwoThenCounterOnOther context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        UUID otherId = permanentId.equals(context.firstPermanentId())
                ? context.secondPermanentId()
                : context.firstPermanentId();

        destructionSupport.sacrificeAndLog(gameData, chosen, context.sacrificingPlayerId());

        Permanent other = gameQueryService.findPermanentById(gameData, otherId);
        if (other != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData,
                    new StackEntry(context.sourceCard(), context.controllerId()), other,
                    com.github.laxika.magicalvibes.model.CounterType.MINUS_ONE_MINUS_ONE, 1);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /** Cannibalize: the spell's controller chooses which target to exile; the other gets two +1/+1 counters. */
    public void handleCannibalizeChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.CannibalizeChoice context) {
        Permanent exiled = gameQueryService.findPermanentById(gameData, permanentId);
        if (exiled == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        UUID otherId = permanentId.equals(context.firstPermanentId())
                ? context.secondPermanentId()
                : context.firstPermanentId();

        exileSupport.exilePermanentAndLog(gameData, exiled, context.sourceCard().getName());

        Permanent other = gameQueryService.findPermanentById(gameData, otherId);
        if (other != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData,
                    new StackEntry(context.sourceCard(), context.controllerId()), other,
                    com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 2);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Barrin's Spite: the creatures' controller has picked which target to sacrifice; the other
     * target returns to its owner's hand.
     */
    public void handleSacrificeOneOfTwoThenReturnOtherToHand(GameData gameData, UUID permanentId,
                                                             PermanentChoiceContext.SacrificeOneOfTwoThenReturnOtherToHand context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        UUID otherId = permanentId.equals(context.firstPermanentId())
                ? context.secondPermanentId()
                : context.firstPermanentId();

        destructionSupport.sacrificeAndLog(gameData, chosen, context.sacrificingPlayerId());

        Permanent other = gameQueryService.findPermanentById(gameData, otherId);
        if (other != null && permanentRemovalService.removePermanentToHand(gameData, other)) {
            gameLogService.append(gameData, GameLog.cardThen(other.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id,
                    other.getCard().getName(), context.sourceCard().getName());
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Valleymaker's mana ability: the activating player has chosen {@code chosenPlayerId} as the
     * recipient; add the produced mana to that player's pool. The activating player retains priority
     * (mana abilities don't change priority), so we simply resume the auto-pass loop afterward.
     */
    public void handleManaAbilityAddToChosenPlayer(GameData gameData, UUID chosenPlayerId,
                                                   PermanentChoiceContext.ManaAbilityAddToChosenPlayer context) {
        com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(chosenPlayerId);
        if (pool != null && context.amount() > 0) {
            pool.add(context.color(), context.amount());
            if (context.creatureSource()) {
                pool.addCreatureMana(context.color(), context.amount());
            }
        }
        String playerName = gameData.playerIdToName.get(chosenPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + context.amount() + " "
                + context.color().getCode() + " from " + context.sourceCardName() + "."));

        inputCompletionService.sbaThenAutoPassWithoutResumingParkedResolution(gameData);
    }

    public void handleCloneCopy(GameData gameData, UUID permanentId) {
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, permanentId);
        if (targetPerm == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        cloneService.completeCloneEntry(gameData, permanentId);

        if (!gameData.interaction.isAwaitingInput()) {
            stateBasedActionService.performStateBasedActions(gameData);

            if (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
                triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            // A clone choice can also arise mid-resolution (token-copy effects), so the canonical
            // epilogue must run to resume the parked entry.
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleTurnFaceUpCopy(GameData gameData, UUID chosenId,
                                     PermanentChoiceContext.TurnFaceUpCopy context) {
        turnFaceUpCopyService.completeChoice(gameData, context.sourcePermanentId(), context.controllerId(), chosenId);
    }

    public void handleCipherEncode(GameData gameData, UUID permanentId) {
        cipherSupport.encode(gameData, permanentId);
    }

    public void handleAttachEquipmentToCreature(GameData gameData, UUID creatureId,
                                                PermanentChoiceContext.AttachEquipmentToCreature context) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, context.equipmentPermanentId());
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (equipment != null && creature != null
                && equipSupport.canAttachEquipment(gameData, equipment, creature)) {
            UUID oldAttachedTo = equipment.getAttachedTo();
            gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(
                    gameData, equipment, oldAttachedTo, creature.getId());
            equipSupport.expireAttachedCopyEffects(gameData, equipment);
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
            gameLogService.append(gameData, GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
        }
        // Begun from a library-search resume (Stonehewer Giant) while the search's stack entry is
        // still parked — the canonical epilogue resumes it.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCreateTokensAndAttachEquipment(GameData gameData, UUID chosenId,
                                                     PermanentChoiceContext.CreateTokensAndAttachEquipment context) {
        createTokensAndAttachEquipmentSupport.handleChoice(gameData, chosenId, context);
    }

    public void handleAuraGraft(GameData gameData, UUID permanentId, PermanentChoiceContext.AuraGraft auraGraft) {
        Permanent aura = gameQueryService.findPermanentById(gameData, auraGraft.auraPermanentId());
        if (aura == null) {
            throw new IllegalStateException("Aura permanent no longer exists");
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(permanentId);
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
        log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, permanentId);

        // Begun mid-resolution (Aura Graft's own spell entry is parked) — canonical epilogue resumes it.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAttachAllAurasToAnotherPermanent(GameData gameData, UUID permanentId,
                                                       PermanentChoiceContext.AttachAllAurasToAnotherPermanent ctx) {
        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        for (UUID auraId : ctx.auraPermanentIds()) {
            Permanent aura = gameQueryService.findPermanentById(gameData, auraId);
            if (aura == null) {
                continue;
            }
            gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
            aura.setAttachedTo(permanentId);
            // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
            aura.setTimestamp(gameData.nextTimestamp());
            gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
            triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, permanentId);
        }

        // A moved control Aura (e.g. Control Magic) grants control of its new host to the Aura's controller.
        creatureControlService.recomputeControl(gameData, newTarget);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleReattachSourceAuraAfterSacrifice(GameData gameData, UUID permanentId,
                                                       PermanentChoiceContext.ReattachSourceAuraAfterSacrifice ctx) {
        Permanent aura = gameQueryService.findPermanentById(gameData, ctx.auraPermanentId());
        if (aura == null) {
            throw new IllegalStateException("Aura permanent no longer exists");
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        // Sacrifice the enchanted permanent, then move the Aura onto the chosen creature or land.
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, ctx.permanentToSacrificeId());
        if (toSacrifice != null) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, ctx.permanentToSacrificeId());
            permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(permanentId);
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
        log.info("Game {} - {} reattached to {} after sacrifice", gameData.id,
                aura.getCard().getName(), newTarget.getCard().getName());

        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, permanentId);

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAttachSourceAuraToChosenPermanent(GameData gameData, UUID permanentId,
                                                        PermanentChoiceContext.AttachSourceAuraToChosenPermanent ctx) {
        Permanent aura = gameQueryService.findPermanentById(gameData, ctx.auraPermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (aura == null || target == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(target.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", target.getCard(), "."));
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, target.getId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePutCounterOnEitherTarget(GameData gameData, UUID permanentId,
                                                PermanentChoiceContext.PutCounterOnEitherTarget context) {
        putCounterOnEitherTargetEffectHandler.placeCounter(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleTransformChosenPermanent(GameData gameData, UUID permanentId) {
        transformChosenPermanentEffectHandler.transform(gameData, permanentId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAttachTargetAuraToAnotherPermanentOfSameType(GameData gameData, UUID permanentId,
                                                                    PermanentChoiceContext.AttachTargetAuraToAnotherPermanentOfSameType ctx) {
        attachTargetAuraHandler.attachChosen(gameData, permanentId, ctx);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLegendRule(GameData gameData, UUID playerId, UUID permanentId, PermanentChoiceContext.LegendRule legendRule) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> toRemove = new ArrayList<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                toRemove.add(perm);
            }
        }
        for (Permanent perm : toRemove) {
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " is put into the graveyard (legend rule)."));
            log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // The removals can cascade (a lost anthem making marked damage lethal) and another legend
        // violation may still exist — the epilogue re-runs the CR 704.3 check, which repeats until
        // settled, and resumes any resolution parked by a mid-effect legend check (many normalfx
        // handlers that put legendaries onto the battlefield call checkLegendRule directly).
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreature(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = sacrificeCreature.sacrificingPlayerId();
        Card sacrificedCard = target.getCard();
        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , sacrificedCard, "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, sacrificedCard.getName());

        // Collect both ally-permanent-sacrificed and global creature-sacrificed triggers; this
        // interactive single-creature-choice path bypasses DestructionSupport.sacrificeAndLog.
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);

        // The choice was begun mid-resolution (e.g. Fleshbag Marauder's "each player sacrifices"),
        // so the standard epilogue must run: it resumes the parked resolution entry — otherwise
        // the spell's remaining effects are silently dropped and the park dangles forever.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDestroyChosenCreature(GameData gameData, UUID permanentId,
                                            PermanentChoiceContext.DestroyChosenCreature context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        if (context.exile()) {
            Card exiledCard = target.getCard();
            permanentRemovalService.removePermanentToExile(gameData, target);
            gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));
            log.info("Game {} - {} exiles {}", gameData.id, context.sourceCardName(), exiledCard.getName());
            permanentRemovalService.removeOrphanedAuras(gameData);
        } else {
            destructionSupport.tryDestroyAndLog(gameData, target, context.sourceCardName());
        }

        // Begun mid-resolution (opponent/target-player-chooses-creature-to-destroy effects) —
        // same parked-resolution resume requirement as handleSacrificeCreature above.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeOtherCreatureThenRevealUntilLowerManaValue(
            GameData gameData, UUID permanentId,
            PermanentChoiceContext.SacrificeOtherCreatureThenRevealUntilLowerManaValue context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        sacrificeOtherCreatureThenRevealHandler.resolveAfterChoice(
                gameData, context.sourceCard(), context.controllerId(), creature, context.predicate());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleExileCombatOpponent(GameData gameData, UUID permanentId,
                                          PermanentChoiceContext.ExileCombatOpponent context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            exileSupport.exilePermanentAndTrackWithSource(
                    gameData, target, context.sourcePermanentId(), context.sourceCard());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDefendingPlayerChoosesCreatureToBlock(GameData gameData, UUID permanentId,
                                                            PermanentChoiceContext.DefendingPlayerChoosesCreatureToBlock context) {
        defendingPlayerChoosesCreatureToBlockEffectHandler.completeChoice(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleBalduvianWarlordChoosesAttacker(GameData gameData, UUID permanentId,
                                                      PermanentChoiceContext.BalduvianWarlordChoosesAttacker context) {
        balduvianWarlordEffectHandler.completeChoice(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleBlightCreatureChoice(GameData gameData, UUID permanentId,
                                           PermanentChoiceContext.BlightCreatureChoice context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }
        StackEntry sourceEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                context.sourceCard(),
                context.controllerId(),
                context.sourceCard().getName() + "'s ability",
                new ArrayList<>(),
                0,
                context.sourcePermanentId());
        blightEffectHandler.placeCountersAndQueueThen(gameData, sourceEntry, creature, context.effect());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleEachOpponentBlightsCreature(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachOpponentBlightsCreature context) {
        eachOpponentBlightsEffectHandler.completeChoice(gameData, permanentId, context);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleEachTargetPlayerLosesLifeAndSacrificesCreature(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature context) {
        eachTargetPlayerLosesLifeAndSacrificesCreatureEffectHandler.completeChoice(gameData, permanentId, context);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleOpponentChoosesCreatureYouGainControl(GameData gameData, UUID permanentId,
            PermanentChoiceContext.OpponentChoosesCreatureYouGainControl context) {
        eachOpponentChoosesCreatureYouGainControlEffectHandler.completeChoice(gameData, permanentId, context);

        // More opponents may still need to choose — leave the parked resolution until all are done.
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleChooseOpponentGainsControlOfSource(GameData gameData, UUID playerId,
            PermanentChoiceContext.ChooseOpponentGainsControlOfSource context) {
        chooseOpponentGainsControlOfSourceEffectHandler.completeChoice(gameData, playerId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleChooseOpponentForPermanentSacrifice(GameData gameData, UUID playerId,
            PermanentChoiceContext.ChooseOpponentForPermanentSacrifice context) {
        opponentChoosesPermanentToSacrificeEffectHandler.completeOpponentChoice(gameData, playerId, context);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleOpponentChoosesPermanentToSacrifice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.OpponentChoosesPermanentToSacrifice context) {
        opponentChoosesPermanentToSacrificeEffectHandler.completePermanentChoice(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCuratorOpponentChoice(GameData gameData, UUID opponentId) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        if (state == null || state.disposition() != CardPileDisposition.HAND_WITH_FACE_DOWN_PILE) {
            throw new IllegalStateException("No pending Curator of Destinies pile choice");
        }

        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), opponentId,
                state.allPermanentIds(), state.cards(), state.cardOwners(), state.pile1Ids(), state.pile2Ids(),
                state.disposition(), state.controllerChoosesPile()));
        playerInputService.beginMultiGraveyardChoice(gameData, state.controllerId(), state.cards(), state.cards().size(),
                "Look at the cards and select cards for the face-up pile (unselected cards form the face-down pile).");
    }

    /** Echo Chamber: the opponent picked which of their creatures the controller gets a copy of. */
    public void handleOpponentChoosesCreatureTheyControlToCopy(GameData gameData, UUID permanentId,
            PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy context) {
        opponentChoosesCreatureTheyControlTokenCopyEffectHandler.completeChoice(gameData, permanentId, context);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAwakenTheMaelstromPermanentCopyChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.AwakenTheMaelstromPermanentCopyChoice context) {
        createTokenCopyOfChosenPermanentYouControlEffectHandler.completeChoice(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAwakenTheMaelstromCounterCreatureChoice(GameData gameData, UUID permanentId) {
        if (gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.setChosenPermanentId(permanentId);
        }
        gameData.rerunCurrentEffectAfterInteraction = true;
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleOpponentMayGainControlOfCreatureYouControl(GameData gameData, UUID permanentId,
            PermanentChoiceContext.OpponentMayGainControlOfCreatureYouControl context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null) {
            gameLogService.append(gameData,
                    GameLog.text(context.sourceCardName() + "'s ability has no effect (source left the battlefield)."));
        } else {
            var controlEffect = new GainControlOfTargetEffect(context.duration());
            creatureControlService.applyControlEffect(gameData, context.choosingOpponentId(), target,
                    controlEffect, context.duration().toEffectDuration(), context.sourcePermanentId(), context.sourceCardName());
            log.info("Game {} - {} gains control of {} via {}", gameData.id,
                    gameData.playerIdToName.get(context.choosingOpponentId()),
                    target.getCard().getName(), context.sourceCardName());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureThenSearchLibrary(GameData gameData, UUID permanentId,
                                                         PermanentChoiceContext.SacrificeCreatureThenSearchLibrary context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = context.sacrificingPlayerId();
        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // "If you do" — sacrifice happened, now search library for a creature card
        librarySearchSupport.searchLibraryForCreatureToHand(gameData, sacrificingPlayerId);

        // When the search awaits input, the library-choice completion owns the epilogue.
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleSacrificeCreatureOpponentsLoseLife(GameData gameData, UUID permanentId,
                                                         PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = context.sacrificingPlayerId();

        // Capture effective power before removing from battlefield (static bonuses still apply)
        int power = gameQueryService.getEffectivePower(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // Each opponent loses life equal to the sacrificed creature's power
        destructionSupport.applyOpponentsLoseLife(gameData, sacrificingPlayerId, power, context.sourceCardName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleForcedCostOrElse(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.ForcedCostOrElse context) {
        destructionSupport.completeForcedCostOrElse(gameData, permanentId, context);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleMaySacrificeForCounterOnSource(GameData gameData, UUID permanentId,
                                                     PermanentChoiceContext.MaySacrificeForCounterOnSource context) {
        maySacrificeForCounterSupport.sacrificeThenAddCounter(
                gameData, context.controllerId(), permanentId, context.sourcePermanentId(), context.counterType());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleGargantuanGorillaSacrificeForest(GameData gameData, UUID permanentId,
                                                       PermanentChoiceContext.GargantuanGorillaSacrificeForest context) {
        gargantuanGorillaUpkeepSupport.sacrificeForest(
                gameData, context.controllerId(), permanentId, context.sourcePermanentId());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /** Desecration Demon: the accepting opponent picked which creature to sacrifice. */
    public void handleAnyOpponentSacrificeCreatureForTapAndCounter(GameData gameData, UUID permanentId,
                                                                   PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndCounter context) {
        anyOpponentSacrificeForTapAndCounterHandler.sacrifice(gameData, context.sacrificingPlayerId(), permanentId);
        anyOpponentSacrificeForTapAndCounterHandler.advance(
                gameData, context.sourceCard(), context.effect(), context.sacrificingPlayerId(), true);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureControllerGainsLifeEqualToToughness(GameData gameData, UUID permanentId,
                                                                            PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = context.sacrificingPlayerId();

        // Capture effective toughness before removing from battlefield (static bonuses still apply)
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        lifeSupport.applyGainLife(gameData, context.lifeGainerId(), toughness, context.sourceCardName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleActivatedAbilityCostChoice(GameData gameData, Player player, UUID permanentId, PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
        abilityActivationService.completeActivatedAbilityCostChoice(gameData, player, costChoice, permanentId);
    }

    public void handleGraveyardAbilityCostChoice(GameData gameData, Player player, UUID permanentId, PermanentChoiceContext.GraveyardAbilityCostChoice graveyardCostChoice) {
        abilityActivationService.completeGraveyardAbilityCostChoice(gameData, player, graveyardCostChoice, permanentId);
    }

    public void handleMayAbilityTapCostChoice(GameData gameData, Player player, UUID permanentId,
                                              PermanentChoiceContext.MayAbilityTapCostChoice mayTapCostChoice) {
        mayAbilityTapCostService.completeTapCostChoice(gameData, player, mayTapCostChoice, permanentId);
    }

    public void handleChoosePlayerThenReturnCreatureToHand(
            GameData gameData, UUID chosenPlayerId,
            PermanentChoiceContext.ChoosePlayerThenReturnCreatureToHand context) {
        List<UUID> creatureIds = gameData.playerBattlefields.getOrDefault(chosenPlayerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(chosenPlayerId));
        playerInputService.beginPermanentChoice(gameData, chosenPlayerId, creatureIds,
                context.sourceCardName() + " — Choose a creature you control to return to its owner's hand.");
    }

    public void handleBounceCreature(GameData gameData, UUID permanentId,
                                     PermanentChoiceContext.BounceCreature context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        StackEntry resolvingEntry = gameData.pendingEffectResolutionEntry;
        boolean resolveFollowUp = context.thenEffect() != null
                && (context.thenCondition() == null || resolvingEntry != null
                && predicateEvaluationService.matchesPermanentPredicate(
                target,
                context.thenCondition(),
                FilterContext.of(gameData)
                        .withSourceCardId(resolvingEntry.getCard().getId())
                        .withSourceControllerId(resolvingEntry.getControllerId())));

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by bounce effect", gameData.id, target.getCard().getName());
        }

        if (resolveFollowUp && resolvingEntry != null) {
            resolvingEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, List.of(context.thenEffect()));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleBouncePermanentThen(GameData gameData, UUID permanentId,
                                           PermanentChoiceContext.BouncePermanentThen context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id,
                    target.getCard().getName(), context.sourceCard().getName());

            if (context.thenEffect() != null) {
                List<CardEffect> thenEffects = new ArrayList<>(List.of(context.thenEffect()));
                var targetSpec = context.thenEffect().targetSpec();
                boolean needsTarget = targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                        || targetSpec.admits(TargetPredicate.Kind.PLAYER);
                if (needsTarget) {
                    List<UUID> validPermanentTargets = new ArrayList<>();
                    if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
                        TargetPredicate declared = targetSpec.targetPredicate();
                        FilterContext filterContext = new FilterContext(
                                gameData, context.sourceCard().getId(), context.controllerId(), null, null);
                        for (UUID playerId : gameData.orderedPlayerIds) {
                            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                            if (battlefield == null) {
                                continue;
                            }
                            for (Permanent permanent : battlefield) {
                                if (targetPredicateEvaluationService.matchesPermanent(
                                        declared, permanent, filterContext)) {
                                    validPermanentTargets.add(permanent.getId());
                                }
                            }
                        }
                    }
                    List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                            ? new ArrayList<>(gameData.orderedPlayerIds)
                            : List.of();
                    if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
                        gameLogService.append(gameData,
                                GameLog.cardThen(context.sourceCard(), "'s ability has no valid targets."));
                    } else {
                        gameData.interaction.setPermanentChoiceContext(
                                new PermanentChoiceContext.MayAbilityTriggerTarget(
                                        context.sourceCard(), context.controllerId(), thenEffects));
                        playerInputService.beginAnyTargetChoice(gameData, context.controllerId(),
                                validPermanentTargets, validPlayerTargets,
                                context.sourceCard().getName() + " — Choose any target.");
                        return;
                    }
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            context.sourceCard(),
                            context.controllerId(),
                            context.sourceCard().getName() + "'s effect",
                            thenEffects,
                            null,
                            context.sourcePermanentId()));
                }
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleMayReturnPermanentToHandAndEnterWithCounters(
            GameData gameData, UUID permanentId,
            PermanentChoiceContext.MayReturnPermanentToHandAndEnterWithCounters context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(),
                context.controllerId(),
                context.sourceCard().getName() + "'s ability",
                List.of(context.effect()),
                permanentId,
                context.sourcePermanentId());
        mayReturnPermanentToHandAndEnterWithCountersEffectHandler.resolve(
                gameData, entry, context.effect());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleBounceOwnPermanentOrSacrificeSelf(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by bounce-or-sacrifice effect", gameData.id, target.getCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeOwnPermanentOrSacrificeSelf(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is sacrificed."));
        log.info("Game {} - {} sacrificed by sacrifice-or-sacrifice effect", gameData.id, target.getCard().getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * "If this land would enter, sacrifice an untapped [land] instead. If you do, put it onto the
     * battlefield. If you don't, put it into its owner's graveyard." (Balduvian Trading Post.)
     * The controller's own player id is offered as the decline option.
     */
    public void handleSacrificePermanentToEnter(GameData gameData, UUID permanentId,
                                                 PermanentChoiceContext.SacrificePermanentToEnter context) {
        boolean declined = context.controllerId().equals(permanentId);
        if (!declined) {
            Permanent sacrifice = gameQueryService.findPermanentById(gameData, permanentId);
            if (sacrifice == null) {
                throw new IllegalStateException("Target permanent no longer exists");
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, sacrifice);
            permanentRemovalService.removeOrphanedAuras(gameData);
            gameLogService.append(gameData, GameLog.cardThen(sacrifice.getCard(), " is sacrificed."));
        }

        battlefieldEntryService.completeSacrificePermanentToEnter(
                gameData, context.controllerId(), context.enteringPermanent(), !declined);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleChampionCreature(GameData gameData, UUID championedPermanentId,
                                       PermanentChoiceContext.ChampionCreature context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, championedPermanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        Card card = target.getOriginalCard();
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), context.controllerId());

        permanentRemovalService.removePermanentToExile(gameData, target);

        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", source.getCard(), "."));
        log.info("Game {} - {} champions {} (exiled until source leaves)",
                gameData.id, source.getCard().getName(), card.getName());

        gameData.addExileReturnOnPermanentLeave(source.getId(), new PendingExileReturn(card, ownerId));

        permanentRemovalService.removeOrphanedAuras(gameData);

        // "When a creature is championed with this permanent, ..." (e.g. Mistbind Clique).
        List<CardEffect> championedEffects = source.getCard().getEffects(EffectSlot.ON_CHAMPIONED);
        if (championedEffects != null && !championedEffects.isEmpty()) {
            beginChampionedTrigger(gameData, source, context.controllerId(), new ArrayList<>(championedEffects));
            return;
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /** Populate (CR 701.36a): the chosen creature token is copied. */
    public void handlePopulate(GameData gameData, UUID chosenPermanentId, PermanentChoiceContext.Populate context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature token no longer exists");
        }

        populateSupport.createCopy(gameData, context.controllerId(), chosen);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePutControlledCreatureOnTopOfLibrary(GameData gameData, UUID chosenPermanentId,
                                                          PermanentChoiceContext.PutControlledCreatureOnTopOfLibrary context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        if (permanentRemovalService.removePermanentToLibraryTop(gameData, chosen)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(chosen.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library (chosen)", gameData.id, chosen.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePatternMatcherCreatureChoice(GameData gameData, UUID chosenPermanentId,
                                                   PermanentChoiceContext.PatternMatcherCreatureChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, chosenPermanentId))
                || !gameQueryService.isCreature(gameData, chosen)
                || chosenPermanentId.equals(context.sourcePermanentId())) {
            throw new IllegalStateException("Chosen permanent is not another creature you control");
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Pattern Matcher resolution is no longer pending");
        }

        patternMatcherHandler.search(gameData, pendingEntry, chosen.getCard().getName());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePolymorphousRushCreatureChoice(GameData gameData, UUID chosenPermanentId,
                                                     PermanentChoiceContext.PolymorphousRushCreatureChoice context) {
        makeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffectHandler.completeChoice(
                gameData, chosenPermanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSoulbondChoosePartner(GameData gameData, UUID partnerPermanentId,
                                            PermanentChoiceContext.SoulbondChoosePartner context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        Permanent partner = gameQueryService.findPermanentById(gameData, partnerPermanentId);
        if (source == null || partner == null
                || !soulbondSupport.isUnpairedCreature(gameData, source)
                || !soulbondSupport.isUnpairedCreature(gameData, partner)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        UUID sourceController = gameQueryService.findPermanentController(gameData, source.getId());
        UUID partnerController = gameQueryService.findPermanentController(gameData, partner.getId());
        if (!context.controllerId().equals(sourceController) || !context.controllerId().equals(partnerController)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        soulbondSupport.pair(gameData, source, partner);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void beginChampionedTrigger(GameData gameData, Permanent source, UUID controllerId,
                                        List<CardEffect> effects) {
        stateBasedActionService.performStateBasedActions(gameData);

        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData, effects, source.getCard().getTargetFilter(), controllerId,
                source.getCard(), TriggerTargetCollector.Options.END_STEP);

        if (result.validTargets().isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s championed trigger has no valid targets."));
            log.info("Game {} - {} championed trigger skipped (no valid targets)",
                    gameData.id, source.getCard().getName());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChampionedTriggerTarget(
                        source.getCard(), controllerId, effects, source.getId()));
        playerInputService.beginPermanentChoice(gameData, controllerId, result.validTargets(),
                source.getCard().getName() + "'s ability — Choose target player.");

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s championed trigger — choose target player."));
        log.info("Game {} - {} championed trigger awaiting target selection", gameData.id, source.getCard().getName());
    }

    public void handlePreventDamageSourceChoice(GameData gameData, UUID permanentId, PermanentChoiceContext.PreventDamageSourceChoice preventSource) {
        Card chosenSource = findDamageSourceCard(gameData, permanentId);
        if (chosenSource == null) {
            throw new IllegalStateException("Chosen source no longer exists");
        }

        UUID controllerId = preventSource.controllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenSource.getName();

        if (preventSource.controllerOnly()) {
            gameData.playerSourceDamagePreventionIds
                    .computeIfAbsent(controllerId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(permanentId);
            if (preventSource.gainLifeForBlackOrRedSource()) {
                gameData.playerSourceDamagePreventionLifeGainIds
                        .computeIfAbsent(controllerId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                        .add(permanentId);
            }
            gameLogService.append(gameData, GameLog.textCardText("All damage ", chosenSource,
                    " would deal to " + playerName + " is prevented this turn."));
        } else {
            gameData.permanentsPreventedFromDealingDamage.add(permanentId);
            gameLogService.append(gameData, GameLog.textCardText("All damage ", chosenSource,
                    " would deal this turn is prevented."));
        }

        log.info("Game {} - {} chose {} as prevented damage source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleGuardDogsPermanentChoice(GameData gameData, UUID permanentId) {
        guardDogsHandler.completeChoice(gameData, permanentId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRedirectDamageSourceChoice(GameData gameData, UUID permanentId,
                                                  PermanentChoiceContext.RedirectDamageSourceChoice redirectSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = redirectSource.controllerId();
        gameData.sourceDamageRedirectShields.add(new SourceDamageRedirectShield(
                controllerId, permanentId, redirectSource.amount(), redirectSource.redirectTargetId()));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.textCardText("The next " + redirectSource.amount() + " damage ",
                chosenPermanent.getCard(),
                " would deal to " + playerName + " or permanents " + playerName + " controls is dealt to another target instead."));
        log.info("Game {} - {} chose {} as redirect damage source (up to {} damage redirected)",
                gameData.id, playerName, chosenPermanent.getCard().getName(), redirectSource.amount());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRedirectCreatureDamageSourceChoice(GameData gameData, UUID permanentId,
                                                         PermanentChoiceContext.RedirectCreatureDamageSourceChoice redirectSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        int redirectAmount = redirectSource.nextEventOnly()
                ? CreatureDamageRedirectShield.NEXT_EVENT
                : CreatureDamageRedirectShield.UNLIMITED;
        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                redirectSource.protectedCreatureId(), permanentId,
                redirectAmount, redirectSource.redirectTargetId()));

        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, redirectSource.protectedCreatureId());
        String protectedName = protectedPerm != null ? protectedPerm.getCard().getName() : "target creature";
        // The redirect target may be a player (Jade Monolith) or a permanent (Oracle's Attendants).
        String redirectName = gameData.playerIdToName.get(redirectSource.redirectTargetId());
        if (redirectName == null) {
            Permanent redirectPerm = gameQueryService.findPermanentById(gameData, redirectSource.redirectTargetId());
            redirectName = redirectPerm != null ? redirectPerm.getCard().getName() : "another creature";
        }
        String prefix = redirectSource.nextEventOnly() ? "The next time " : "All damage ";
        String suffix = redirectSource.nextEventOnly()
                ? " would deal damage to " + protectedName + " this turn, that damage is dealt to " + redirectName + " instead."
                : " would deal to " + protectedName + " this turn is dealt to " + redirectName + " instead.";
        gameLogService.append(gameData, GameLog.textCardText(prefix, chosenPermanent.getCard(), suffix));
        log.info("Game {} - {} chose {} as creature damage redirect source", gameData.id,
                gameData.playerIdToName.get(redirectSource.controllerId()), chosenPermanent.getCard().getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRedirectPlayerDamageSourceChoice(GameData gameData, UUID permanentId,
                                                       PermanentChoiceContext.RedirectPlayerDamageSourceChoice redirectSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        gameData.playerSourceNextDamageRedirectShields.add(new PlayerSourceNextDamageRedirectShield(
                redirectSource.controllerId(), permanentId, redirectSource.redirectTargetId()));

        Permanent destination = gameQueryService.findPermanentById(gameData, redirectSource.redirectTargetId());
        String destinationName = destination != null ? destination.getCard().getName() : "the target creature";
        gameLogService.append(gameData, GameLog.textCardText("The next time ", chosenPermanent.getCard(),
                " would deal damage to you this turn, that damage is dealt to " + destinationName + " instead."));
        log.info("Game {} - {} chose {} as player damage redirect source", gameData.id,
                gameData.playerIdToName.get(redirectSource.controllerId()), chosenPermanent.getCard().getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventDamageToTargetFromSourceChoice(GameData gameData, UUID permanentId,
                                                             PermanentChoiceContext.PreventDamageToTargetFromSourceChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID targetId = ctx.targetId();
        gameData.targetSourceDamagePreventionShields.add(new TargetSourceDamagePreventionShield(
                targetId, permanentId, ctx.amount()));

        // Determine target name for logging
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
        String targetName = targetPerm != null
                ? targetPerm.getCard().getName()
                : gameData.playerIdToName.getOrDefault(targetId, "unknown");

        
        gameLogService.append(gameData, GameLog.builder().text("The next " + ctx.amount() + " damage ").card(chosenPermanent.getCard()).text(" would deal to " + targetName + " is prevented.").build());
        log.info("Game {} - Chose {} as damage source, preventing up to {} damage to {}",
                gameData.id, chosenPermanent.getCard().getName(), ctx.amount(), targetName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventNextDamageFromSourceChoice(GameData gameData, UUID permanentId,
                                                        PermanentChoiceContext.PreventNextDamageFromSourceChoice ctx) {
        Card chosenSource = findDamageSourceCard(gameData, permanentId);
        if (chosenSource == null) {
            throw new IllegalStateException("Chosen source no longer exists");
        }

        UUID controllerId = ctx.controllerId();
        boolean gainLife = ctx.gainLife();
        gameData.playerSourceNextDamageShields.add(new PlayerSourceNextDamageShield(
                controllerId, permanentId, gainLife, false, false, ctx.exileFromLibrary(),
                ctx.damageSourceControllerCard(), ctx.preventHalfDamage(), ctx.drawCards(),
                findDamageSourceController(gameData, permanentId)));

        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenSource.getName();
        String rider;
        if (ctx.drawCards() && ctx.damageSourceControllerCard() != null) {
            rider = " and " + ctx.damageSourceControllerCard().getName()
                    + " deals that much damage to the source's controller, and " + playerName
                    + " draws that many cards.";
        } else if (ctx.drawCards()) {
            rider = " and " + playerName + " draws that many cards.";
        } else if (gainLife) {
            rider = " and " + playerName + " gains that much life.";
        } else if (ctx.exileFromLibrary()) {
            rider = " and " + playerName + " exiles that many cards from the top of their library.";
        } else if (ctx.damageSourceControllerCard() != null) {
            rider = " and " + ctx.damageSourceControllerCard().getName()
                    + " deals that much damage to the source's controller.";
        } else {
            rider = ".";
        }
        String logEntry = "The next time " + sourceName + " would deal damage to " + playerName
                + " this turn, " + (ctx.preventHalfDamage()
                ? "half that damage, rounded down, is prevented."
                : "it is prevented" + rider);
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chose {} as next-damage prevention source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private UUID findDamageSourceController(GameData gameData, UUID sourceId) {
        UUID permanentController = gameQueryService.findPermanentController(gameData, sourceId);
        if (permanentController != null) {
            return permanentController;
        }
        return gameData.stack.stream()
                .filter(entry -> entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                .filter(entry -> entry.getCard().getId().equals(sourceId))
                .map(StackEntry::getControllerId)
                .findFirst()
                .orElse(null);
    }

    public void handlePreventNextDamageFromSourceToYouAndYourCreaturesChoice(
            GameData gameData, UUID permanentId,
            PermanentChoiceContext.PreventNextDamageFromSourceToYouAndYourCreaturesChoice ctx) {
        Card chosenSource = findDamageSourceCard(gameData, permanentId);
        if (chosenSource == null) {
            throw new IllegalStateException("Chosen source no longer exists");
        }

        UUID controllerId = ctx.controllerId();
        gameData.playerSourceNextDamageShields.add(
                new PlayerSourceNextDamageShield(controllerId, permanentId, true, true, true, false));

        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenSource.getName();
        gameLogService.append(gameData, GameLog.text("The next time " + sourceName + " would deal damage to "
                + playerName + " and/or creatures they control this turn, it is prevented."));
        log.info("Game {} - {} chose {} as Shadowbane next-damage prevention source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleEyeForAnEyeSourceChoice(GameData gameData, UUID permanentId,
                                              PermanentChoiceContext.EyeForAnEyeSourceChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = ctx.controllerId();
        gameData.eyeForAnEyeShields.add(new com.github.laxika.magicalvibes.model.EyeForAnEyeShield(
                controllerId, permanentId, ctx.eyeCard(), controllerId));

        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenPermanent.getCard().getName();
        String logEntry = "The next time " + sourceName + " would deal damage to " + playerName
                + " this turn, that much damage is also dealt to " + sourceName + "'s controller.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chose {} as Eye for an Eye reflection source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRedirectNextDamageFromChosenSourceToPermanentChoice(GameData gameData, UUID permanentId,
                                                                          PermanentChoiceContext.RedirectNextDamageFromChosenSourceToPermanentChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        gameData.sourceNextDamageRedirectToPermanentShields.add(
                new com.github.laxika.magicalvibes.model.SourceNextDamageRedirectToPermanentShield(
                        permanentId, ctx.destinationPermanentId()));

        Permanent destination = gameQueryService.findPermanentById(gameData, ctx.destinationPermanentId());
        String sourceName = chosenPermanent.getCard().getName();
        String destinationName = destination != null ? destination.getCard().getName() : "it";
        gameLogService.append(gameData, GameLog.text("The next time " + sourceName + " would deal damage this turn, "
                + "that damage is dealt to " + destinationName + " instead."));
        log.info("Game {} - {} chose {} as damage redirect source", gameData.id,
                gameData.playerIdToName.get(ctx.controllerId()), sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleReflectDamageToSourceControllerChoice(GameData gameData, UUID permanentId,
                                                            PermanentChoiceContext.ReflectDamageToSourceControllerChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        gameData.reflectDamageToSourceControllerShields.add(permanentId);

        String sourceName = chosenPermanent.getCard().getName();
        gameLogService.append(gameData, GameLog.text("The next time " + sourceName + " would deal damage this turn, "
                + "that damage is dealt to " + sourceName + "'s controller instead."));
        log.info("Game {} - {} chose {} as Reflect Damage source", gameData.id,
                gameData.playerIdToName.get(ctx.controllerId()), sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventNextDamageFromSourceToPermanentChoice(GameData gameData, UUID permanentId,
                                                                    PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice ctx) {
        Card chosenSource = findDamageSourceCard(gameData, permanentId);
        if (chosenSource == null) {
            throw new IllegalStateException("Chosen source no longer exists");
        }

        gameData.sourceNextDamageToAnyTargetShields.add(
                com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield.forRecipient(
                        permanentId, ctx.protectedPermanentId()));

        Permanent protectedPermanent = gameQueryService.findPermanentById(gameData, ctx.protectedPermanentId());
        String protectedName = protectedPermanent != null ? protectedPermanent.getCard().getName() : "the enchanted creature";
        gameLogService.append(gameData, GameLog.textCardText("The next time ", chosenSource,
                " would deal damage to " + protectedName + " this turn, that damage is prevented."));
        log.info("Game {} - {} chose {} as next-damage-to-permanent prevention source", gameData.id,
                gameData.playerIdToName.get(ctx.controllerId()), chosenSource.getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventNextDamageFromSourceToAnyTargetChoice(GameData gameData, UUID permanentId,
                                                                   PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice ctx) {
        Card chosenSource = findDamageSourceCard(gameData, permanentId);
        if (chosenSource == null) {
            throw new IllegalStateException("Chosen source no longer exists");
        }

        gameData.sourceNextDamageToAnyTargetShields.add(ctx.damageRedSourceController()
                ? new com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield(
                        permanentId, true, ctx.passageCard(), ctx.controllerId())
                : new com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield(permanentId));

        String sourceName = chosenSource.getName();
        String logEntry = ctx.damageRedSourceController()
                ? "The next time " + sourceName + " would deal damage to any target this turn, it is prevented."
                        + " If it is red, Honorable Passage deals that much damage to its controller."
                : "The next time " + sourceName + " would deal damage to any target this turn, it is prevented.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chose {} as next-damage-to-any-target prevention source", gameData.id,
                gameData.playerIdToName.get(ctx.controllerId()), sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private Card findDamageSourceCard(GameData gameData, UUID sourceId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
        if (permanent != null) {
            return permanent.getCard();
        }
        return gameData.stack.stream()
                .filter(entry -> entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                .filter(entry -> entry.getCard().getId().equals(sourceId))
                .map(StackEntry::getCard)
                .findFirst()
                .orElse(null);
    }

    /**
     * Desperate Gambit: the source has been chosen, so flip the coin now. A won flip installs the
     * doubling variant of the same one-shot any-target shield; a lost flip installs the plain
     * prevention variant.
     */
    public void handleDoubleOrPreventNextDamageFromSourceChoice(GameData gameData, UUID permanentId,
                                                                PermanentChoiceContext.DoubleOrPreventNextDamageFromSourceChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, ctx.controllerId());
        boolean wonFlip = result.heads();
        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        String sourceName = chosenPermanent.getCard().getName();
        gameLogService.append(gameData, GameLog.text(wonFlip
                ? playerName + " wins the coin flip for Desperate Gambit"
                        + coinFlipService.replacementDetails(result) + "."
                : playerName + " loses the coin flip for Desperate Gambit"
                        + coinFlipService.replacementDetails(result) + "."));

        if (wonFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, ctx.controllerId());
        } else {
            triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, ctx.controllerId());
        }

        gameData.sourceNextDamageToAnyTargetShields.add(wonFlip
                ? com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield.doubling(permanentId)
                : new com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield(permanentId));

        gameLogService.append(gameData, GameLog.text(wonFlip
                ? "The next time " + sourceName + " would deal damage this turn, it deals double that damage instead."
                : "The next time " + sourceName + " would deal damage this turn, that damage is prevented."));
        log.info("Game {} - {} chose {} for Desperate Gambit (won flip: {})", gameData.id, playerName, sourceName,
                wonFlip);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeArtifactForDividedDamage(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
        Permanent artifactToSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (artifactToSacrifice == null) {
            throw new IllegalStateException("Artifact permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, artifactToSacrifice);

        String playerName = gameData.playerIdToName.get(sadd.controllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , artifactToSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for divided damage", gameData.id, playerName, artifactToSacrifice.getCard().getName());

        damageSupport.dealDividedDamageToAnyTargets(
                gameData, sadd.sourceCard(), sadd.controllerId(), sadd.damageAssignments());

        gameData.pendingETBDamageAssignments = Map.of();

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeAnotherCreatureDealPowerDamage(GameData gameData, UUID permanentId,
                                                              PermanentChoiceContext.SacrificeAnotherCreatureDealPowerDamage ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective power before removing from battlefield (static bonuses still apply;
        // CR 510.1a clamps negative power to 0).
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, toSacrifice));

        UUID sourcePermanentId = gameData.playerBattlefields.get(ctx.controllerId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(ctx.sourceCard().getId()))
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        queuePowerDamageReflexiveTrigger(
                gameData, ctx.controllerId(), ctx.sourceCard(), sourcePermanentId, power);
    }

    private void queuePowerDamageReflexiveTrigger(GameData gameData, UUID controllerId,
                                                   Card sourceCard, UUID sourcePermanentId,
                                                   int power) {
        CardEffect damageEffect = new DealDamageToAnyTargetEffect(power);
        TargetPredicate targetPredicate = damageEffect.targetSpec().targetPredicate();
        FilterContext filterContext = new FilterContext(
                gameData, sourceCard.getId(), controllerId, null, null);
        List<UUID> validPermanentTargets = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (targetPredicateEvaluationService.matchesPermanent(
                        targetPredicate, permanent, filterContext)) {
                    validPermanentTargets.add(permanent.getId());
                }
            }
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.MayAbilityTriggerTarget(
                        sourceCard, controllerId, List.of(damageEffect), sourcePermanentId, null));
        playerInputService.beginAnyTargetChoice(
                gameData, controllerId, validPermanentTargets,
                new ArrayList<>(gameData.orderedPlayerIds),
                sourceCard.getName() + " â€” Choose any target.");
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " â€” choose a target for the reflexive trigger."));
    }

    public void handleSacrificeAnotherCreatureGainLifeAndDraw(GameData gameData, UUID permanentId,
                                                             PermanentChoiceContext.SacrificeAnotherCreatureGainLifeAndDraw ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective power before removing from battlefield (static bonuses still apply;
        // negative power counts as 0).
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, toSacrifice));

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        if (power > 0) {
            lifeSupport.applyGainLife(gameData, ctx.controllerId(), power, ctx.sourceCard().getName());
            for (int i = 0; i < power; i++) {
                drawService.resolveDrawCard(gameData, ctx.controllerId());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureThenMassDamageEqualToPower(
            GameData gameData, UUID permanentId,
            PermanentChoiceContext.SacrificeCreatureThenMassDamageEqualToPower context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        sacrificeCreatureThenMassDamageHandler.resolveAfterChoice(gameData, chosen, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleExploitSacrifice(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.ExploitSacrifice ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), " for exploit."));
        log.info("Game {} - {} sacrifices {} for {} exploit", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        // CR 702.110: "exploits a creature" only if the exploit permanent was on the battlefield
        // as the ability started resolving (sacrificing itself still counts).
        if (ctx.sourceStillOnBattlefield()) {
            triggerCollectionService.checkExploitTriggers(
                    gameData, ctx.sourceCard(), ctx.controllerId(), ctx.sourcePermanentId());
            if (gameData.hasPendingInteraction(PermanentChoiceContext.ExploitTriggerTarget.class)
                    && !gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.processNextExploitTriggerTarget(gameData);
            }
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAnyPlayerMaySacrificeLandPutSourceOnTop(
            GameData gameData, UUID permanentId,
            PermanentChoiceContext.AnyPlayerMaySacrificeLandPutSourceOnTop context) {
        anyPlayerMaySacrificeLandHandler.sacrificeLand(
                gameData, context.sacrificingPlayerId(), permanentId);
        anyPlayerMaySacrificeLandHandler.putSourceOnTop(
                gameData, context.sourceCard(), context.effect());
        anyPlayerMaySacrificeLandHandler.advance(
                gameData, context.sourceCard(), context.effect(), context.sacrificingPlayerId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificePermanentThen(GameData gameData, UUID permanentId,
                                              PermanentChoiceContext.SacrificePermanentThen ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID sourcePermanentId = gameData.playerBattlefields.get(ctx.controllerId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(ctx.sourceCard().getId()))
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);

        int sacrificedPower = gameQueryService.getEffectivePower(gameData, toSacrifice);
        int sacrificedToughness = gameQueryService.getEffectiveToughness(gameData, toSacrifice);
        Permanent sacrificedSnapshot = new Permanent(toSacrifice);
        StackEntry originalEntry = gameData.pendingEffectResolutionEntry;
        if (originalEntry != null) {
            originalEntry.setSacrificedPermanentSnapshot(sacrificedSnapshot);
            originalEntry.setSacrificedPower(sacrificedPower);
            originalEntry.setSacrificedToughness(sacrificedToughness);
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , toSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        if (!ctx.reflexive()) {
            if (ctx.thenEffect() != null) {
                if (originalEntry == null) {
                    throw new IllegalStateException("No pending effect resolution for synchronous sacrifice follow-up");
                }
                originalEntry.insertEffectsToResolve(
                        gameData.pendingEffectResolutionIndex, List.of(ctx.thenEffect()));
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Execute the "if/when you do" effect by pushing it onto the stack as a triggered ability.
        // A null thenEffect means a bare "sacrifice a permanent" with no follow-up.
        // When the rider needs a target (Sorin, Imperious Bloodlord: "When you do, … deals 3 damage
        // to any target"), choose the target as the reflexive trigger goes on the stack.
        if (ctx.thenEffect() != null) {
            List<CardEffect> thenEffects = new ArrayList<>(List.of(ctx.thenEffect()));
            var targetSpec = ctx.thenEffect().targetSpec();
            boolean needsTarget = targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                    || targetSpec.admits(TargetPredicate.Kind.PLAYER);
            if (needsTarget) {
                List<UUID> validPermanentTargets = new ArrayList<>();
                if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
                    // The rider's own declared target decides which permanents are legal — Sorin's
                    // is "any target" (CR 115.4), so the restriction is evaluated rather than
                    // re-implemented here and stays layer-aware (CR 613.1d).
                    TargetPredicate declared = targetSpec.targetPredicate();
                    FilterContext filterContext = new FilterContext(
                            gameData, ctx.sourceCard().getId(), ctx.controllerId(), null, null);
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) {
                            continue;
                        }
                        for (Permanent p : battlefield) {
                            if (targetPredicateEvaluationService.matchesPermanent(declared, p, filterContext)) {
                                validPermanentTargets.add(p.getId());
                            }
                        }
                    }
                }
                List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                        ? new ArrayList<>(gameData.orderedPlayerIds)
                        : List.of();
                if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(ctx.sourceCard(), "'s ability has no valid targets."));
                    log.info("Game {} - {} sacrifice-then rider skipped (no valid targets)",
                            gameData.id, ctx.sourceCard().getName());
                } else {
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.MayAbilityTriggerTarget(
                                    ctx.sourceCard(), ctx.controllerId(), thenEffects));
                    playerInputService.beginAnyTargetChoice(gameData, ctx.controllerId(),
                            validPermanentTargets, validPlayerTargets,
                            ctx.sourceCard().getName() + " — Choose any target.");
                    gameLogService.append(gameData,
                            GameLog.cardThen(ctx.sourceCard(),
                                    " — choose a target for the reflexive trigger."));
                    log.info("Game {} - {} sacrifice-then rider awaiting any-target",
                            gameData.id, ctx.sourceCard().getName());
                    return;
                }
            } else {
                List<UUID> targetCardIds = originalEntry == null || originalEntry.getTargetCardIds() == null
                        ? List.of() : new ArrayList<>(originalEntry.getTargetCardIds());
                StackEntry triggeredEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ctx.sourceCard(),
                        ctx.controllerId(),
                        ctx.sourceCard().getName() + "'s effect",
                        thenEffects,
                        0,
                        null,
                        sourcePermanentId,
                        Map.of(),
                        null,
                        targetCardIds,
                        List.of()
                );
                triggeredEntry.setSacrificedPermanentSnapshot(sacrificedSnapshot);
                triggeredEntry.setSacrificedPower(sacrificedPower);
                triggeredEntry.setSacrificedToughness(sacrificedToughness);
                gameData.stack.add(triggeredEntry);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificePermanentAndReturnTargetCards(
            GameData gameData, UUID permanentId,
            PermanentChoiceContext.SacrificePermanentAndReturnTargetCards ctx) {
        sacrificePermanentAndReturnHandler.resolveAfterChoice(gameData, permanentId, ctx);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificePermanentAndBoostSelf(GameData gameData, UUID permanentId,
                                                     PermanentChoiceContext.SacrificePermanentAndBoostSelf ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        Permanent source = ctx.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId());
        if (source == null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
            if (battlefield != null) {
                source = battlefield.stream()
                        .filter(permanent -> permanent.getOriginalCard().getId().equals(ctx.sourceCard().getId()))
                        .findFirst()
                        .orElse(null);
            }
        }
        if (source != null) {
            source.setPowerModifier(source.getPowerModifier() + ctx.power());
            source.setToughnessModifier(source.getToughnessModifier() + ctx.toughness());
            source.getGrantedKeywords().addAll(ctx.grantedKeywords());
            gameLogService.append(gameData, GameLog.builder()
                    .card(source.getCard())
                    .text(String.format(" gets %+d/%+d until end of turn.", ctx.power(), ctx.toughness()))
                    .build());
            log.info("Game {} - {} gets {}/{}", gameData.id, source.getCard().getName(),
                    ctx.power(), ctx.toughness());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificePermanentAndGrantKeywordSelf(GameData gameData, UUID permanentId,
                                                            PermanentChoiceContext.SacrificePermanentAndGrantKeywordSelf ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        Permanent source = ctx.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId());
        if (source == null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
            if (battlefield != null) {
                source = battlefield.stream()
                        .filter(permanent -> permanent.getOriginalCard().getId().equals(ctx.sourceCard().getId()))
                        .findFirst()
                        .orElse(null);
            }
        }
        if (source != null) {
            source.getGrantedKeywords().addAll(ctx.keywords());
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    ctx.sourceCard().getName(),
                    null,
                    ctx.controllerId(),
                    new GrantKeywordEffect(ctx.keywords(), GrantScope.SELF),
                    source.getId(),
                    null,
                    null,
                    EffectDuration.UNTIL_END_OF_TURN,
                    0));
            String keywordNames = formatKeywords(ctx.keywords());
            gameLogService.append(gameData, GameLog.builder()
                    .card(source.getCard())
                    .text(" gains " + keywordNames + " until end of turn.")
                    .build());
            log.info("Game {} - {} gains {}", gameData.id, source.getCard().getName(), ctx.keywords());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.name().charAt(0)
                        + keyword.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((first, second) -> first + ", " + second)
                .orElse("");
    }

    /**
     * Torment of Hailfire: {@code ctx.playerId()} sacrifices the nonland permanent they chose, then the
     * paused spell resumes ({@code TormentOfHailfireEffectHandler} re-runs to advance to the next opponent).
     */
    public void handleTormentSacrifice(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.TormentSacrifice ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice != null) {
            destructionSupport.sacrificeAndLog(gameData, toSacrifice, ctx.playerId());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureCreateTokensEqualToToughness(GameData gameData, UUID permanentId,
                                                                    PermanentChoiceContext.SacrificeCreatureCreateTokensEqualToToughness ctx) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective toughness before removing from battlefield (static bonuses still apply)
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                target.getCard().getName(), ctx.sourceCard().getName());

        // Create X tokens, where X is the sacrificed creature's toughness
        if (toughness > 0) {
            CreateTokenEffect sized = ctx.tokenTemplate().withAmount(toughness);
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ctx.sourceCard(),
                    ctx.controllerId(),
                    ctx.sourceCard().getName() + "'s effect",
                    new ArrayList<>(List.of(sized))
            ));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureCreateSizedTokenEqualToPower(GameData gameData, UUID permanentId,
                                                                    PermanentChoiceContext.SacrificeCreatureCreateSizedTokenEqualToPower ctx) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective power before removing from battlefield (static bonuses still apply)
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, target));

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                target.getCard().getName(), ctx.sourceCard().getName());

        // Create one token whose power and toughness are each equal to the sacrificed creature's power
        CreateTokenEffect t = ctx.tokenTemplate();
        CreateTokenEffect sized = new CreateTokenEffect(
                t.primaryType(), 1, t.tokenName(), power, power,
                t.color(), t.colors(), t.subtypes(), t.keywords(), t.additionalTypes(),
                t.tappedAndAttacking(), t.tapped(), t.tokenEffects(), t.tokenAbilities(),
                t.exileAtEndOfCombat(), t.exileAtEndStep(), t.legendary(), t.initialPlusOnePlusOneCounters(),
                t.grantedKeywordsUntilEndOfTurn(), t.supertypes());
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ctx.sourceCard(),
                ctx.controllerId(),
                ctx.sourceCard().getName() + "'s effect",
                new ArrayList<>(List.of(sized))
        ));

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureCreateTokensIfSubtype(GameData gameData, UUID permanentId,
                                                             PermanentChoiceContext.SacrificeCreatureCreateTokensIfSubtype ctx) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        sacrificeCreatureCreateTokensIfSubtypeHandler.sacrificeAndMaybeCreateTokens(
                gameData, ctx.sacrificingPlayerId(), target, ctx.requiredSubtype(),
                ctx.tokenTemplate(), ctx.sourceCard().getSetCode());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleTariffTieBreak(GameData gameData, UUID permanentId,
                                     PermanentChoiceContext.TariffTieBreak context) {
        tariffSupport.handleTieBreakChosen(gameData, permanentId, context);
    }

    public void handleJuxtaposeTieBreak(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.JuxtaposeTieBreak context) {
        juxtaposeSupport.handleTieBreakChosen(gameData, permanentId, context);
    }

    public void handleChooseCreatureAsEnter(GameData gameData, UUID chosenCreatureId,
                                             PermanentChoiceContext.ChooseCreatureAsEnter context) {
        Permanent entering = gameQueryService.findPermanentById(gameData, context.enteringPermanentId());
        if (entering == null) {
            throw new IllegalStateException("Entering permanent no longer exists");
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenCreatureId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        entering.setChosenPermanentId(chosenCreatureId);

        gameLogService.append(gameData, GameLog.cardTextCard(entering.getCard(), " chooses ", chosen.getCard(), "."));
        log.info("Game {} - {} chooses {}", gameData.id,
                entering.getCard().getName(), chosen.getCard().getName());

        // "Enchanted creature is a copy of the chosen creature" (Metamorphic Alteration) applies as
        // soon as the choice is made, before the Aura's ETB effects run.
        auraCopyService.applyChosenCreatureCopy(gameData, entering);

        battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                context.targetId(), context.wasCastFromHand(), context.etbMode(), context.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handlePendingAuraPlacement(GameData gameData, UUID playerId, UUID permanentId) {
        Card auraCard = gameData.interaction.consumePendingAuraCard();
        UUID auraOwnerId = gameData.interaction.consumePendingAuraOwnerId();
        // If an explicit aura owner was set (e.g. Necrotic Plague), use it instead of the chooser
        UUID auraControllerId = auraOwnerId != null ? auraOwnerId : playerId;

        Permanent enchantTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (enchantTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (gameData.warpWorldOperation.sourceName != null) {
            gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                    new WarpWorldEnchantmentPlacement(auraControllerId, auraCard, enchantTarget.getId())
            );

            if (!gameData.warpWorldOperation.pendingAuraChoices.isEmpty()) {
                warpWorldService.beginNextPendingWarpWorldAuraChoice(gameData);
                return;
            }
            warpWorldService.placePendingWarpWorldEnchantments(gameData);
            if (!gameData.pendingLibraryBottomReorders.isEmpty()) {
                warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
                return;
            }
            warpWorldService.finalizePendingWarpWorld(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        } else {
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(enchantTarget.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, auraPerm);

            boolean hasControlEffect = auraCard.getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
            if (hasControlEffect) {
                creatureControlService.applyControlEffect(gameData, auraControllerId, enchantTarget,
                        new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                        auraPerm.getId(), auraCard.getName());
            }

            String playerName = gameData.playerIdToName.get(auraControllerId);
            
            gameLogService.append(gameData, GameLog.builder().card(auraCard).text(" enters the battlefield attached to ").card(enchantTarget.getCard()).text(" under " + playerName + "'s control.").build());
            log.info("Game {} - {} puts {} onto battlefield attached to {}",
                    gameData.id, playerName, auraCard.getName(), enchantTarget.getCard().getName());
        }

        // Aura placements are begun by normalfx handlers mid-resolution (including Warp World,
        // whose own entry is parked while the choices run) — the epilogue resumes the parked entry.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
