package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingCapriciousEfreetState;
import com.github.laxika.magicalvibes.model.PendingBendOrBreak;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PendingWhimsOfTheFates;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseTwoCreaturesByPowerDifferenceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnNControlledPermanentsToHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.WormsOfTheEarthEffectHandler;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles multi-permanent choice inputs where the player selects
 * zero or more permanents from a list.
 *
 * <p>Covers exile-damaged-player-permanent, sacrifice-self-to-destroy,
 * sacrifice attacking creatures, combat damage bounce, awakening counter
 * placement, proliferate, and tap-subtype-boost (e.g. Myr Battlesphere).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiPermanentChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentChoiceTriggerHandlerService triggerHandler;
    private final TurnProgressionService turnProgressionService;
    private final com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService battlefieldEntryService;
    private final DestructionSupport destructionSupport;
    private final DamageSupport damageSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.FightOrFlightSupport fightOrFlightSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.StandOrFallSupport standOrFallSupport;
    private final CreatureControlService creatureControlService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AnimationSupport animationSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.FlickerEffectHandler flickerEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ExileAndCloakDisguisedCreaturesEffectHandler
            exileAndCloakDisguisedCreaturesEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ExileSelfAndSaddledCreatureEffectHandler
            exileSelfAndSaddledCreatureEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokenCopiesOfSaddledCreatureEffectHandler
            createTokenCopiesOfSaddledCreatureEffectHandler;
    private final ChooseTwoCreaturesByPowerDifferenceEffectHandler chooseTwoCreaturesByPowerDifferenceEffectHandler;
    private final ReturnNControlledPermanentsToHandEffectHandler returnNControlledPermanentsToHandEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .CreateTokenCopiesOfChosenDistinctControlledTokensEffectHandler distinctTokenCopyHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .ExileAnyNumberOfPermanentsUntilSourceLeavesEffectHandler exileUntilSourceLeavesHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .AttachAnyNumberOfControlledEquipmentToTargetCreatureEffectHandler
            attachAnyNumberOfControlledEquipmentHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .DestroyUpToOneAttachedPermanentEffectHandler destroyUpToOneAttachedPermanentHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PutMatchingPermanentsOnTopOfOwnersLibrariesEffectHandler putMatchingPermanentsOnTopOfOwnersLibrariesEffectHandler;
    private final LifeSupport lifeSupport;
    private final DamagePreventionService damagePreventionService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport permanentControlSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport tapUntapSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.QueueReflexiveAbilityEffectHandler
            queueReflexiveAbilityEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.MakeCreatureUnblockableEffectHandler
            makeCreatureUnblockableEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport librarySearchSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.KillingWaveEffectHandler killingWaveEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffectHandler
            powerLimitedCreatureChoiceHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachCreatureControllerSacrificesPermanentUnlessPaysEffectHandler fadeAwayEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EquipoiseSupport equipoiseSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .ChooseKeptPermanentOfEachTypeThenSacrificeRestEffectHandler keepOneOfEachTypeHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffectHandler winnowingHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .DestroyUpToOneNonbasicLandPerPlayerThenSearchEffectHandler
            destroyUpToOneNonbasicLandPerPlayerThenSearchHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerSacrificesOneOfEachTypeEffectHandler eachPlayerSacrificesOneOfEachTypeHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .TargetPlayerSacrificesCreatureAndPlaneswalkerEffectHandler
            targetPlayerSacrificesCreatureAndPlaneswalkerHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachOpponentSacrificesArtifactAndNonartifactCreatureEffectHandler
            eachOpponentSacrificesArtifactAndNonartifactCreatureHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffectHandler
            eachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffectHandler eachPlayerChoosesLandOfEachBasicTypeHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffectHandler
            eachPlayerChoosesLandOfEachBasicTypeThenReturnToHandHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .ChooseLandOfEachBasicTypeThenDestroyEffectHandler chooseLandOfEachBasicTypeThenDestroyHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerReturnsCreatureToHandEffectHandler eachPlayerReturnsCreatureToHandHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .EachPlayerChoosesLandsThenDestroyRestEffectHandler eachPlayerChoosesLandsThenDestroyRestHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .RaidingPartyEffectHandler raidingPartyEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.BendOrBreakEffectHandler bendOrBreakEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.WhimsOfTheFatesEffectHandler
            whimsOfTheFatesEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffectHandler
            tappedLandSacrificeDamageIfSubtypeHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.CulturalExchangeSupport
            culturalExchangeSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final WormsOfTheEarthEffectHandler wormsOfTheEarthEffectHandler;
    private final AbilityActivationService abilityActivationService;

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class) == null) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        PendingInteraction.MultiPermanentChoice multiPermanentChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        if (multiPermanentChoice == null || !player.getId().equals(multiPermanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<UUID> validIds = multiPermanentChoice.validIds();
        List<UUID> validPlayerIds = multiPermanentChoice.validPlayerIds();
        Set<UUID> validSelectionIds = new HashSet<>(validIds);
        validSelectionIds.addAll(validPlayerIds);
        int maxCount = multiPermanentChoice.maxCount();

        if (permanentIds == null) {
            permanentIds = List.of();
        }

        // Validate before touching interaction state: a rejected answer must leave the prompt
        // standing so the player can answer again. Clearing first and then throwing destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry, wedging
        // the game (and with it deferPlayerLossCheck) on nothing worse than a stale client answer
        // — a permanent that died between prompt and answer is an ordinary race.
        if (permanentIds.size() > maxCount) {
            throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
        }

        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }

        for (UUID permId : permanentIds) {
            if (!validSelectionIds.contains(permId)) {
                throw new IllegalStateException("Invalid selection: " + permId);
            }
        }

        MultiPermanentChoiceContext context = multiPermanentChoice.context();
        if (context instanceof MultiPermanentChoiceContext.SagaChapterTargetSelection sagaTarget
                && permanentIds.size() < sagaTarget.minTargets()) {
            throw new IllegalStateException("Too few targets selected");
        }
        if ((context instanceof MultiPermanentChoiceContext.EachPlayerSacrificeOneOfEachTypeChoice
                || context instanceof MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeChoice
                || context instanceof MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice
                || context instanceof MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice)
                && permanentIds.size() != 1) {
            throw new IllegalStateException("Exactly one permanent must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.EachPlayerReturnsCreature
                && permanentIds.size() != 1) {
            throw new IllegalStateException("Exactly one creature must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.DestroyRestChoice choice
                && choice.requiresChoice() && permanentIds.size() != 1) {
            throw new IllegalStateException("Exactly one permanent must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.EachPlayerChoosesLandsThenDestroyRestChoice choice
                && permanentIds.size() != choice.requiredCount()) {
            throw new IllegalStateException("Exactly " + choice.requiredCount()
                    + " lands must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.UntapPermanentsForAmount choice
                && permanentIds.size() != choice.requiredCount()) {
            throw new IllegalStateException("Exactly " + choice.requiredCount()
                    + " permanents must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.WormsOfTheEarthSacrificeLands
                && permanentIds.size() != 2) {
            throw new IllegalStateException("Exactly two lands must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.WormsOfTheEarthSacrificeLands worms
                && permanentIds.stream().anyMatch(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null
                            || !worms.playerId().equals(gameQueryService.findPermanentController(gameData, id))
                            || !gameQueryService.isLand(gameData, permanent);
                })) {
            throw new IllegalStateException("A selected permanent is no longer a land you control");
        }
        if (context instanceof MultiPermanentChoiceContext.CulturalExchange culturalExchange
                && !culturalExchange.firstSelection()
                && permanentIds.size() != culturalExchange.firstChosenIds().size()) {
            throw new IllegalStateException("Must select exactly "
                    + culturalExchange.firstChosenIds().size() + " creatures");
        }
        if (context instanceof MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource powerCtx
                && !permanentIds.isEmpty()
                && totalEffectivePower(gameData, permanentIds) < powerCtx.requiredPower()) {
            // Not a partial cost: the set either reaches the threshold or is not a legal choice,
            // so leave the prompt standing rather than sacrificing creatures for nothing.
            throw new IllegalStateException("Selected creatures have total power below "
                    + powerCtx.requiredPower());
        }
        if (context instanceof MultiPermanentChoiceContext.EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice powerCtx
                && totalPower(gameData, permanentIds) > powerCtx.maxPower()) {
            throw new IllegalStateException("Selected creatures have total power above " + powerCtx.maxPower());
        }
        if (context instanceof MultiPermanentChoiceContext.DealDamageToDamagedPlayerControls
                && permanentIds.size() != 1) {
            throw new IllegalStateException("Exactly one creature must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.RedirectDamageToChosenPermanent ctx) {
            if (permanentIds.size() != 1) {
                throw new IllegalStateException("Exactly one creature or planeswalker must be selected");
            }
            Permanent selected = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (selected == null
                    || !ctx.protectedPlayerId().equals(gameQueryService.findPermanentController(gameData, selected.getId()))
                    || (!gameQueryService.isCreature(gameData, selected)
                    && !gameQueryService.isPlaneswalker(gameData, selected))) {
                throw new IllegalStateException("Selected permanent is no longer a creature or planeswalker you control");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.ChooseTwoCreaturesByPowerDifference
                && permanentIds.size() != 2) {
            throw new IllegalStateException("Exactly two creatures must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.ReturnNControlledPermanentsToHand returnContext
                && permanentIds.size() != returnContext.effect().count()) {
            throw new IllegalStateException("Exactly " + returnContext.effect().count()
                    + " permanents must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.TargetPlayerSacrificesCreatureAndPlaneswalker ctx) {
            if (permanentIds.size() != ctx.requiredCount()) {
                throw new IllegalStateException("Must select exactly " + ctx.requiredCount()
                        + " permanents to sacrifice");
            }
            if (ctx.requiredCount() == 2) {
                boolean hasCreature = permanentIds.stream().anyMatch(ctx.creatureIds()::contains);
                boolean hasPlaneswalker = permanentIds.stream().anyMatch(ctx.planeswalkerIds()::contains);
                if (!hasCreature || !hasPlaneswalker) {
                    throw new IllegalStateException("The selection must include a creature and a planeswalker");
                }
            }
        }
        if (context instanceof MultiPermanentChoiceContext.EachOpponentSacrificesArtifactAndNonartifactCreature ctx) {
            if (permanentIds.size() != ctx.requiredCount()) {
                throw new IllegalStateException("Must select exactly " + ctx.requiredCount()
                        + " creatures to sacrifice");
            }
            if (ctx.requiredCount() == 2) {
                boolean hasArtifactCreature = permanentIds.stream().anyMatch(ctx.artifactCreatureIds()::contains);
                boolean hasNonartifactCreature = permanentIds.stream().anyMatch(ctx.nonartifactCreatureIds()::contains);
                if (!hasArtifactCreature || !hasNonartifactCreature) {
                    throw new IllegalStateException(
                            "The selection must include an artifact creature and a nonartifact creature");
                }
            }
        }

        if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsToEnter enterCtx) {
            if (!permanentIds.isEmpty() && permanentIds.size() != enterCtx.requiredCount()) {
                throw new IllegalStateException("Must select exactly " + enterCtx.requiredCount()
                        + " permanents or none to decline");
            }
            if (permanentIds.stream().anyMatch(id -> gameQueryService.findPermanentById(gameData, id) == null)) {
                throw new IllegalStateException("A selected permanent no longer exists");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsOrElse sacrificeCtx) {
            if (!permanentIds.isEmpty() && permanentIds.size() != sacrificeCtx.requiredCount()) {
                throw new IllegalStateException("Must select exactly " + sacrificeCtx.requiredCount()
                        + " permanents or none to decline");
            }
            if (permanentIds.stream().anyMatch(id -> {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                return permanent == null || !playerId.equals(gameQueryService.findPermanentController(gameData, id));
            })) {
                throw new IllegalStateException("A selected permanent is no longer controlled by the chooser");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.TapPermanentsForAmount tapCtx) {
            if (permanentIds.size() != tapCtx.requiredCount()) {
                throw new IllegalStateException("Must select exactly " + tapCtx.requiredCount()
                        + " permanents");
            }
            if (permanentIds.stream().anyMatch(id -> {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                return permanent == null || permanent.isTapped();
            })) {
                throw new IllegalStateException("A selected permanent is no longer untapped");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.TapPermanentsDrawPerTapped
                && permanentIds.stream().anyMatch(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null || permanent.isTapped();
                })) {
            throw new IllegalStateException("A selected permanent is no longer untapped");
        }
        if (context instanceof MultiPermanentChoiceContext.TapCreaturesThenQueueReflexiveAbility
                && permanentIds.stream().anyMatch(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null || permanent.isTapped()
                            || !playerId.equals(gameQueryService.findPermanentController(gameData, id))
                            || !gameQueryService.isCreature(gameData, permanent);
                })) {
            throw new IllegalStateException("A selected creature is no longer untapped and controlled by you");
        }
        if (context instanceof MultiPermanentChoiceContext.TapCreaturesBoostSelf
                && permanentIds.stream().anyMatch(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null || permanent.isTapped() || !gameQueryService.isCreature(gameData, permanent);
                })) {
            throw new IllegalStateException("A selected creature is no longer untapped");
        }
        if (context instanceof MultiPermanentChoiceContext.TapOtherCreaturesForUnblockable tapCtx
                && !permanentIds.isEmpty()) {
            if (permanentIds.size() != tapCtx.requiredCount()) {
                throw new IllegalStateException("Must select exactly " + tapCtx.requiredCount()
                        + " creatures or none to decline");
            }
            if (permanentIds.stream().anyMatch(id -> {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                return permanent == null || permanent.isTapped()
                        || !gameQueryService.isCreature(gameData, permanent)
                        || !playerId.equals(gameQueryService.findPermanentController(gameData, id))
                        || id.equals(tapCtx.sourcePermanentId());
            })) {
                throw new IllegalStateException(
                        "A selected creature is no longer an eligible other untapped creature you control");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.RaidingPartyTapChoice) {
            if (permanentIds.stream().anyMatch(id -> {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                return permanent == null || permanent.isTapped()
                        || !gameQueryService.isCreature(gameData, permanent)
                        || !gameQueryService.hasColor(gameData, permanent,
                        com.github.laxika.magicalvibes.model.CardColor.WHITE)
                        || !playerId.equals(gameQueryService.findPermanentController(gameData, id));
            })) {
                throw new IllegalStateException("A selected permanent is no longer an untapped white creature you control");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.RaidingPartyPlainsChoice) {
            if (permanentIds.stream().anyMatch(id -> {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                return permanent == null || !gameQueryService.isLand(gameData, permanent)
                        || !gameQueryService.cardHasSubtype(permanent.getCard(),
                        com.github.laxika.magicalvibes.model.CardSubtype.PLAINS,
                        gameData, permanent.getCard().getOwnerId());
            })) {
                throw new IllegalStateException("A selected permanent is no longer a Plains");
            }
        }
        if (context instanceof MultiPermanentChoiceContext.FadeAwaySacrifice fadeAway
                && permanentIds.size() != fadeAway.requiredCount()) {
            throw new IllegalStateException("Must select exactly " + fadeAway.requiredCount()
                    + " permanents to sacrifice");
        }
        if (context instanceof MultiPermanentChoiceContext.PutPermanentsOnTopOfOwnersLibraries
                && !new HashSet<>(permanentIds).equals(new HashSet<>(validIds))) {
            throw new IllegalStateException("All matching permanents must be ordered");
        }
        if (context instanceof MultiPermanentChoiceContext.FlickerAnyNumber
                && permanentIds.stream().anyMatch(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null
                            || !playerId.equals(gameQueryService.findPermanentController(gameData, id))
                            || !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent,
                            ((MultiPermanentChoiceContext.FlickerAnyNumber) context).effect().filter());
                })) {
            throw new IllegalStateException("A selected permanent is no longer an eligible permanent you control");
        }
        if (context instanceof MultiPermanentChoiceContext.RecloakDisguisedCreatures
                && permanentIds.stream().anyMatch(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null
                            || permanent.isFaceDown()
                            || !playerId.equals(gameQueryService.findPermanentController(gameData, id))
                            || !gameQueryService.isCreature(gameData, permanent)
                            || !gameQueryService.hasKeyword(
                            gameData, permanent, Keyword.DISGUISE);
                })) {
            throw new IllegalStateException(
                    "A selected permanent is no longer a face-up creature with disguise you control");
        }
        if (context instanceof MultiPermanentChoiceContext.CreateTokenCopiesOfChosenDistinctControlledTokens) {
            Set<String> chosenNames = new HashSet<>();
            for (UUID permanentId : permanentIds) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (permanent == null || !chosenNames.add(permanent.getCard().getName())) {
                    throw new IllegalStateException("Chosen tokens must have different names");
                }
            }
        }
        if (context instanceof MultiPermanentChoiceContext.SagaChapterCounterDistribution
                && permanentIds.isEmpty()) {
            throw new IllegalStateException("At least one target creature must be selected");
        }
        if (context instanceof MultiPermanentChoiceContext.SacrificeSelfToDestroy
                && permanentIds.isEmpty()) {
            throw new IllegalStateException("A creature target is required after accepting the sacrifice");
        }
        if (context instanceof MultiPermanentChoiceContext.ActivatedAbilityExileArtifactsCost exileArtifactsContext) {
            abilityActivationService.validateActivatedAbilityExileArtifactsChoice(
                    gameData, exileArtifactsContext, permanentIds);
        }
        if (context instanceof MultiPermanentChoiceContext.CounterDistribution
                && permanentIds.isEmpty()) {
            throw new IllegalStateException("At least one target creature must be selected");
        }

        gameData.interaction.clearAwaitingInput();

        if (context instanceof MultiPermanentChoiceContext.ActivatedAbilityExileArtifactsCost exileArtifactsContext) {
            abilityActivationService.completeActivatedAbilityExileArtifactsCostChoice(
                    gameData, player, exileArtifactsContext, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.ExileDamagedPlayerControls) {
            handleExileDamagedPlayerControlsPermanent(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.UpkeepAnyNumberPlayerTargets ctx) {
            triggerHandler.handleUpkeepAnyNumberPlayerTargets(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DealDamageToDamagedPlayerControls ctx) {
            handleDealDamageToDamagedPlayerControls(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyDamagedPlayerControls ctx) {
            handleDestroyDamagedPlayerControlsPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.UntapChosenPermanent ctx) {
            handleUntapChosenPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.RedirectDamageToChosenPermanent ctx) {
            handleRedirectDamageToChosenPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapChosenPermanent ctx) {
            handleTapChosenPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapAnyNumberPermanents) {
            handleTapAnyNumberPermanents(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeDamagedPlayerControls ctx) {
            handleSacrificeDamagedPlayerControlsPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeSelfToDestroy ctx) {
            handleSacrificeSelfToDestroy(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.GainControlOfPermanentAndAssignNoCombatDamage ctx) {
            handleGainControlOfPermanentAndAssignNoCombatDamage(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage ctx) {
            handleDestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TransformAndAttach ctx) {
            handleTransformAndAttach(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TransformAnyNumber ctx) {
            handleTransformAnyNumber(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AttachAnyNumberOfControlledEquipmentToTargetCreature ctx) {
            attachAnyNumberOfControlledEquipmentHandler.completeChoice(gameData, playerId, permanentIds, ctx);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeAttackingCreatures) {
            handleSacrificeAttackingCreature(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.ExileAttackingCreatures) {
            handleExileAttackingCreatures(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary ctx) {
            handlePutAttackingCreaturesOnLibrary(gameData, permanentIds, multiPermanentChoice.validIds(), ctx);
        } else if (context instanceof MultiPermanentChoiceContext.PutPermanentsOnTopOfOwnersLibraries ctx) {
            putMatchingPermanentsOnTopOfOwnersLibrariesEffectHandler.completeChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyCreaturesOpponentControls ctx) {
            handleDestroyCreaturesOpponentControls(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapChosenPermanents ctx) {
            handleTapChosenPermanents(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapPermanentsForAmount ctx) {
            handleTapPermanentsForAmount(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.UntapChosenPermanents ctx) {
            handleUntapChosenPermanents(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.UntapPermanentsForAmount ctx) {
            handleUntapPermanentsForAmount(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ReturnTargetPermanentsToHand) {
            handleReturnTargetPermanentsToHand(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.RemoveCounterFromChosenPermanents ctx) {
            handleRemoveCounterFromChosenPermanents(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ReturnAnyNumberAndRecordCount ctx) {
            handleReturnAnyNumberAndRecordCount(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ReturnNControlledPermanentsToHand ctx) {
            handleReturnNControlledPermanentsToHand(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.FlickerAnyNumber ctx) {
            if (flickerEffectHandler.completeAnyNumberChoice(gameData, permanentIds, ctx)
                    && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
        } else if (context instanceof MultiPermanentChoiceContext.RecloakDisguisedCreatures ctx) {
            exileAndCloakDisguisedCreaturesEffectHandler.completeChoice(gameData, permanentIds, ctx);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.ExileSelfAndSaddledCreature ctx) {
            exileSelfAndSaddledCreatureEffectHandler.completeChoice(gameData, permanentIds, ctx);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.CreateTokenCopiesOfSaddledCreature ctx) {
            createTokenCopiesOfSaddledCreatureEffectHandler.completeChoice(gameData, permanentIds, ctx);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof MultiPermanentChoiceContext.CombatDamageBounce ctx) {
            handleCombatDamageBounce(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AimCounterPlacement) {
            handleAimCounterPlacement(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.OwnPermanentCounterPlacement ctx) {
            handleOwnPermanentCounterPlacement(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.OwnPermanentCounterPlacementByPlayer ctx) {
            handleOwnPermanentCounterPlacementByPlayer(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.OpponentCreatureCounterPlacement ctx) {
            handleOpponentCreatureCounterPlacement(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.OwnPermanentCounterPlacementWithChosenReference ctx) {
            handleOwnPermanentCounterPlacementWithChosenReference(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AwakeningCounterPlacement) {
            handleAwakeningCounterPlacement(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.Proliferate ctx) {
            handleProliferate(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapSubtypeBoost ctx) {
            handleTapSubtypeBoost(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapAnyNumberBoostSelf ctx) {
            handleTapAnyNumberBoostSelf(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesBoostSelf ctx) {
            handleTapCreaturesBoostSelf(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapOtherCreaturesForUnblockable ctx) {
            handleTapOtherCreaturesForUnblockable(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyRestChoice ctx) {
            handleDestroyRestChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedSacrifice ctx) {
            handleForcedSacrifice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.WormsOfTheEarthSacrificeLands ctx) {
            wormsOfTheEarthEffectHandler.sacrificeAndDestroy(
                    gameData, ctx.sourceCard(), ctx.effect(), permanentIds, ctx.playerId());
            permanentRemovalService.removeOrphanedAuras(gameData);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedSacrificeThenDamageIfSubtype ctx) {
            handleForcedSacrificeThenDamageIfSubtype(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedDestroy ctx) {
            handleForcedDestroy(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedReturnToHand ctx) {
            handleForcedReturnToHand(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerReturnsCreature ctx) {
            handleEachPlayerReturnsCreature(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseCreatureRestCantBlock ctx) {
            handleChooseCreatureRestCantBlock(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseCreaturesToAttackNextTurn ctx) {
            handleChooseCreaturesToAttackNextTurn(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.CulturalExchange ctx) {
            culturalExchangeSupport.completeChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesGainLife ctx) {
            handleTapCreaturesGainLife(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesCreateTokens ctx) {
            handleTapCreaturesCreateTokens(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesThenQueueReflexiveAbility ctx) {
            handleTapCreaturesThenQueueReflexiveAbility(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapPermanentsDrawPerTapped) {
            handleTapPermanentsDrawPerTapped(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.TapPermanentsAndPutCounters ctx) {
            handleTapPermanentsAndPutCounters(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeLandsSearchLandsToBattlefieldTapped) {
            handleSacrificeLandsSearchLandsToBattlefieldTapped(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsDrawPerSacrificed) {
            handleSacrificePermanentsDrawPerSacrificed(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsAddManaPerSacrificed ctx) {
            handleSacrificePermanentsAddManaPerSacrificed(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsOrElse ctx) {
            handleSacrificePermanentsOrElse(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount ctx) {
            handleSacrificeAnyNumberAndRecordCount(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource ctx) {
            handleSacrificeCreaturesWithTotalPowerOrSacrificeSource(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice ctx) {
            handlePowerLimitedCreatureChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseFivePermanentsSearchSameNameToBattlefieldTapped) {
            handleChooseFivePermanentsSearchSameName(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.DevourSacrifice ctx) {
            handleDevourSacrifice(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeCreaturesSetEnteringPowerToughness ctx) {
            handleSacrificeCreaturesSetEnteringPowerToughness(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeAsEntersForCounters ctx) {
            handleSacrificeAsEntersForCounters(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsToEnter ctx) {
            handleSacrificePermanentsToEnter(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.PayManaPerCreatureUntap ctx) {
            handlePayManaPerCreatureUntap(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.StaticOrbUntap ctx) {
            handleStaticOrbUntap(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ExileTetraviteTokensPutCountersOnSource ctx) {
            handleExileTetraviteTokensPutCountersOnSource(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.KillingWaveKeep ctx) {
            handleKillingWaveKeep(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.FadeAwayKeep ctx) {
            handleFadeAwayKeep(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.FadeAwaySacrifice ctx) {
            handleFadeAwaySacrifice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.KeepOneOfEachTypeChoice ctx) {
            handleKeepOneOfEachTypeChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.WinnowingChoice ctx) {
            handleWinnowingChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyUpToOneNonbasicLandPerPlayerChoice ctx) {
            handleDestroyUpToOneNonbasicLandPerPlayerChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerSacrificeOneOfEachTypeChoice ctx) {
            handleEachPlayerSacrificeOneOfEachTypeChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TargetPlayerSacrificesCreatureAndPlaneswalker ctx) {
            targetPlayerSacrificesCreatureAndPlaneswalkerHandler.completeChoice(gameData, permanentIds, ctx);
            permanentRemovalService.removeOrphanedAuras(gameData);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.EachOpponentSacrificesArtifactAndNonartifactCreature ctx) {
            eachOpponentSacrificesArtifactAndNonartifactCreatureHandler.completeChoice(gameData, permanentIds, ctx);

            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            permanentRemovalService.removeOrphanedAuras(gameData);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnChoice ctx) {
            eachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnHandler.completeChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeChoice ctx) {
            handleEachPlayerChoosesLandOfEachBasicTypeChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice ctx) {
            handleEachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice ctx) {
            handleChooseLandOfEachBasicTypeThenDestroyChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EachPlayerChoosesLandsThenDestroyRestChoice ctx) {
            handleEachPlayerChoosesLandsThenDestroyRestChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.RaidingPartyTapChoice ctx) {
            handleRaidingPartyTapChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.RaidingPartyPlainsChoice ctx) {
            handleRaidingPartyPlainsChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EquipoisePhaseOut ctx) {
            equipoiseSupport.handleChosen(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseTwoCreaturesByPowerDifference) {
            handleChooseTwoCreaturesByPowerDifference(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.CreateTokenCopiesOfChosenDistinctControlledTokens) {
            handleCreateTokenCopiesOfChosenDistinctControlledTokens(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.ExileAnyNumberUntilSourceLeaves ctx) {
            exileUntilSourceLeavesHandler.completeChoice(gameData, permanentIds, ctx.sourcePermanentId());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyUpToOneAttachedPermanent ctx) {
            destroyUpToOneAttachedPermanentHandler.completeChoice(gameData, permanentIds, ctx);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.SagaChapterTargetSelection ctx) {
            handleSagaChapterTargetSelection(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ExileOwnNontokenCreaturesUntilSourceLeaves ctx) {
            exileUntilSourceLeavesHandler.completeChoice(gameData, permanentIds, ctx.sourcePermanentId());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof MultiPermanentChoiceContext.SagaChapterCounterDistribution ctx) {
            handleSagaChapterCounterDistribution(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.CounterDistribution ctx) {
            handleCounterDistribution(gameData, permanentIds, ctx);
        } else if (gameData.hasPendingInteraction(PendingCapriciousEfreetState.class)) {
            handleCapriciousEfreetOpponentTargets(gameData, permanentIds);
        } else if (gameData.hasPendingInteraction(PendingPileSeparation.class)) {
            handlePileSeparation(gameData, permanentIds);
        } else if (gameData.hasPendingInteraction(PendingBendOrBreak.class)) {
            bendOrBreakEffectHandler.completeLandSeparation(gameData, permanentIds);
        } else if (gameData.hasPendingInteraction(PendingWhimsOfTheFates.class)) {
            whimsOfTheFatesEffectHandler.completePileSelection(gameData, permanentIds);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    private void handleChooseTwoCreaturesByPowerDifference(GameData gameData, List<UUID> permanentIds) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending effect resolution entry");
        }
        chooseTwoCreaturesByPowerDifferenceEffectHandler.completeChoice(gameData, permanentIds, entry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapAnyNumberPermanents(GameData gameData, List<UUID> permanentIds) {
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                tapUntapSupport.tapPermanent(gameData, permanent);
            }
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleCreateTokenCopiesOfChosenDistinctControlledTokens(
            GameData gameData, List<UUID> permanentIds) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending effect resolution entry");
        }
        distinctTokenCopyHandler.completeChoice(gameData, permanentIds, entry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSagaChapterCounterDistribution(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.SagaChapterCounterDistribution context) {
        ChoiceContext.SagaChapterCounterAssignment assignment =
                new ChoiceContext.SagaChapterCounterAssignment(
                        context.sourceCard(), context.controllerId(), context.effects(),
                        context.sourcePermanentId(), context.chapterName(), context.counterType(),
                        permanentIds, java.util.Map.of(), context.total(), 0);
        playerInputService.beginSagaChapterCounterAssignmentChoice(
                gameData, context.controllerId(), assignment);
    }

    private void handleSagaChapterTargetSelection(
            GameData gameData, List<UUID> targetIds,
            MultiPermanentChoiceContext.SagaChapterTargetSelection context) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(),
                context.controllerId(),
                context.sourceCard().getName() + "'s chapter " + context.chapterName() + " ability",
                new ArrayList<>(context.effects()),
                context.sourcePermanentId(),
                new ArrayList<>(targetIds));
        gameData.stack.add(entry);
        triggerCollectionService.checkTargetChoiceTriggers(gameData, entry);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleCounterDistribution(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.CounterDistribution context) {
        ChoiceContext.CounterDistributionAssignment assignment =
                new ChoiceContext.CounterDistributionAssignment(
                        context.sourceCard(), context.controllerId(), context.effects(),
                        context.sourcePermanentId(), context.counterType(), permanentIds,
                        java.util.Map.of(), context.total(), 0);
        playerInputService.beginCounterDistributionAssignmentChoice(
                gameData, context.controllerId(), assignment);
    }

    private void handleSacrificeSelfToDestroy(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                              MultiPermanentChoiceContext.SacrificeSelfToDestroy context) {
        UUID sourcePermId = context.sourcePermanentId();

        if (!permanentIds.isEmpty()) {
            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
            if (source != null) {
                if (permanentRemovalService.removePermanentToGraveyard(gameData, source)) {
                    triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, source.getCard());
                    gameLogService.append(gameData, GameLog.isSacrificed(source.getCard()));
                    log.info("Game {} - {} sacrificed for combat damage trigger", gameData.id, source.getCard().getName());

                    UUID chosenPermId = permanentIds.getFirst();
                    Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
                    if (target != null) {
                        if (permanentRemovalService.tryDestroyPermanent(gameData, target, context.cannotBeRegenerated())) {
                            gameLogService.append(gameData, GameLog.isDestroyed(target.getCard()));
                            log.info("Game {} - {} destroyed by sacrifice trigger", gameData.id, target.getCard().getName());
                        }
                    }
                }

                permanentRemovalService.removeOrphanedAuras(gameData);
            } else {
                String logEntry = "Source creature no longer exists — sacrifice trigger fizzles.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleGainControlOfPermanentAndAssignNoCombatDamage(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                                     MultiPermanentChoiceContext.GainControlOfPermanentAndAssignNoCombatDamage context) {
        UUID sourcePermId = context.sourcePermanentId();
        ControlDuration duration = context.duration();

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(playerId) + " chooses not to gain control of a " + context.choiceNoun() + "."));
        } else {
            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
            UUID sourceController = source != null ? gameQueryService.findPermanentController(gameData, sourcePermId) : null;
            Permanent stolen = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());

            // Per ruling: a source-linked duration only takes control if you still control the
            // source; the "assigns no combat damage" rider applies only when one is actually taken.
            boolean sourceOk = !duration.isSourceLinked() || (source != null && playerId.equals(sourceController));
            if (stolen != null && sourceOk) {
                creatureControlService.applyControlEffect(gameData, playerId, stolen,
                        new GainControlOfTargetEffect(duration), duration.toEffectDuration(),
                        duration.isSourceLinked() ? sourcePermId : null,
                        source != null ? source.getCard().getName() : stolen.getCard().getName());
                if (source != null) {
                    gameData.creaturesPreventedFromDealingCombatDamage.add(sourcePermId);
                    gameLogService.append(gameData,
                            GameLog.cardThen(source.getCard(), " assigns no combat damage this turn."));
                }
                log.info("Game {} - {} gains control of {} and assigns no combat damage",
                        gameData.id, gameData.playerIdToName.get(playerId), stolen.getCard().getName());
            } else {
                gameLogService.append(gameData,
                        GameLog.text("The ability has no effect (source no longer controlled)."));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage context) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses no " + context.choiceNoun() + " to destroy."));
        } else {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null && permanentRemovalService.tryDestroyPermanent(gameData, target, false)) {
                gameLogService.append(gameData, GameLog.isDestroyed(target.getCard()));
                log.info("Game {} - {} destroyed by unblocked-attack trigger", gameData.id, target.getCard().getName());
            }
            permanentRemovalService.removeOrphanedAuras(gameData);

            // The rider applies whether or not the permanent survived — the source still assigns no
            // combat damage this turn once a permanent was chosen.
            Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
            if (source != null) {
                gameData.creaturesPreventedFromDealingCombatDamage.add(context.sourcePermanentId());
                gameLogService.append(gameData,
                        GameLog.cardThen(source.getCard(), " assigns no combat damage this turn."));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTransformAndAttach(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.TransformAndAttach context) {
        UUID sourcePermId = context.sourcePermanentId();

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to attach.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            animationSupport.completeTransformAndAttach(
                    gameData, playerId, sourcePermId, permanentIds.getFirst());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTransformAnyNumber(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.TransformAnyNumber context) {
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null
                    || !playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                    || !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, context.filter())
                    || gameQueryService.isTransformPrevented(gameData, permanent)) {
                continue;
            }
            if (!permanent.isTransformed()) {
                animationSupport.transformToBackFace(gameData, permanent);
            } else {
                animationSupport.transformToFrontFace(gameData, permanent);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleExileDamagedPlayerControlsPermanent(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to exile a permanent.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            UUID chosenPermId = permanentIds.getFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
            if (target != null) {
                permanentRemovalService.removePermanentToExile(gameData, target);
                gameLogService.append(gameData, GameLog.isExiled(target.getCard()));
                log.info("Game {} - {} exiled by combat damage trigger", gameData.id, target.getCard().getName());

                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDealDamageToDamagedPlayerControls(GameData gameData, List<UUID> permanentIds,
                                                         MultiPermanentChoiceContext.DealDamageToDamagedPlayerControls context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
        if (target != null && gameQueryService.isCreature(gameData, target)) {
            StackEntry damageEntry = context.damageEntry();
            Permanent source = damageEntry.getSourcePermanentId() == null
                    ? damageEntry.getSourcePermanentSnapshot()
                    : gameQueryService.findPermanentById(gameData, damageEntry.getSourcePermanentId());
            int damageAmount = amountEvaluationService.evaluate(gameData, context.damage(),
                    AmountContext.forStackEntry(damageEntry, source));
            int damage = gameQueryService.applyDamageMultiplier(gameData, damageAmount, damageEntry);
            if (!damageSupport.isDamagePreventedForCreature(gameData, damageEntry, target)) {
                damageSupport.dealCreatureDamage(gameData, damageEntry, target, damage);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDestroyDamagedPlayerControlsPermanent(GameData gameData, List<UUID> permanentIds,
                                                             MultiPermanentChoiceContext.DestroyDamagedPlayerControls context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                destructionSupport.tryDestroyAndLog(gameData, target, context.sourceName());
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleUntapChosenPermanent(GameData gameData, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.UntapChosenPermanent context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                tapUntapSupport.untapPermanent(gameData, target);
                gameLogService.append(gameData,
                        GameLog.builder().text(context.sourceName() + " untaps ").card(target.getCard()).text(".").build());
                log.info("Game {} - {} untaps {}", gameData.id, context.sourceName(), target.getCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleRedirectDamageToChosenPermanent(
            GameData gameData,
            List<UUID> permanentIds,
            MultiPermanentChoiceContext.RedirectDamageToChosenPermanent context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
        if (target != null) {
            gameData.turnDamageRedirectToCreatureShields.add(
                    com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield
                            .forCreatureOrPlaneswalker(context.protectedPlayerId(), target.getId()));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapChosenPermanent(GameData gameData, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.TapChosenPermanent context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                tapUntapSupport.tapPermanent(gameData, target);
                gameLogService.append(gameData,
                        GameLog.builder().text(context.sourceName() + " taps ").card(target.getCard()).text(".").build());
                log.info("Game {} - {} taps {}", gameData.id, context.sourceName(), target.getCard().getName());

                if (context.preventUntapWhileSourceTapped() && context.sourcePermanentId() != null) {
                    target.getUntapPreventedByPermanentIds().add(context.sourcePermanentId());
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                            " won't untap as long as " + context.sourceName() + " remains tapped."));
                }
                if (context.preventUntapWhileSourceOnBattlefield() && context.sourcePermanentId() != null) {
                    target.getUntapPreventedWhileSourceOnBattlefieldIds().add(context.sourcePermanentId());
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                            " won't untap as long as " + context.sourceName() + " remains on the battlefield."));
                }
                if (context.skipNextUntap()) {
                    target.setSkipUntapCount(target.getSkipUntapCount() + 1);
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                            " won't untap during its controller's next untap step."));
                }
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeDamagedPlayerControlsPermanent(GameData gameData, List<UUID> permanentIds,
                                                               MultiPermanentChoiceContext.SacrificeDamagedPlayerControls context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                String ownerName = controllerId != null ? gameData.playerIdToName.get(controllerId) : "Unknown";
                if (permanentRemovalService.removePermanentToGraveyard(gameData, target)) {
                    if (controllerId != null) {
                        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, controllerId, target.getCard());
                    }
                    gameLogService.append(gameData, GameLog.playerSacrifices(ownerName, target.getCard()));
                    log.info("Game {} - {} sacrificed by {}", gameData.id, target.getCard().getName(), context.sourceName());
                }
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeAttackingCreature(GameData gameData, List<UUID> permanentIds) {
        for (UUID permId : permanentIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, permId);
            if (creature != null) {
                UUID ownerId = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf != null && bf.contains(creature)) {
                        ownerId = pid;
                        break;
                    }
                }
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                String ownerName = ownerId != null ? gameData.playerIdToName.get(ownerId) : "Unknown";
                gameLogService.append(gameData, GameLog.playerSacrifices(ownerName, creature.getCard()));
                log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, creature.getCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleExileAttackingCreatures(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to exile any attacking creatures."));
        } else {
            List<Card> exiledCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, permId);
                if (creature != null) {
                    permanentRemovalService.removePermanentToExile(gameData, creature);
                    exiledCards.add(creature.getCard());
                }
            }
            if (!exiledCards.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                gameLogService.append(gameData,
                        appendCards(GameLog.builder(), exiledCards)
                                .text((exiledCards.size() == 1 ? " is" : " are") + " exiled.").build());
                log.info("Game {} - {} exiles {} attacking creatures", gameData.id,
                        gameData.playerIdToName.get(playerId), exiledCards.size());
            }
        }

        // Resume resolving remaining effects on the same ability (e.g. the cycling draw)
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handlePutAttackingCreaturesOnLibrary(GameData gameData, List<UUID> topCreatureIds,
            List<UUID> currentOwnerCreatureIds,
            MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary context) {
        List<UUID> topIds = new ArrayList<>(context.topCreatureIds());
        topIds.addAll(topCreatureIds);
        Set<UUID> selectedTopIds = new HashSet<>(topCreatureIds);
        List<UUID> bottomIds = new ArrayList<>(context.bottomCreatureIds());
        for (UUID creatureId : currentOwnerCreatureIds) {
            if (!selectedTopIds.contains(creatureId)) {
                bottomIds.add(creatureId);
            }
        }

        continuePutAttackingCreaturesOnLibrary(gameData, context.remainingCreatureIds(), topIds,
                bottomIds, context.sourceCardName());
    }

    private void continuePutAttackingCreaturesOnLibrary(GameData gameData, List<UUID> pendingIds,
            List<UUID> topIds, List<UUID> bottomIds, String sourceCardName) {
        List<UUID> remainingIds = pendingIds.stream()
                .filter(creatureId -> gameQueryService.findPermanentById(gameData, creatureId) != null)
                .toList();
        if (!remainingIds.isEmpty()) {
            Permanent first = gameQueryService.findPermanentById(gameData, remainingIds.getFirst());
            UUID ownerId = ownerId(gameData, first);
            List<UUID> ownerCreatureIds = new ArrayList<>();
            List<UUID> laterCreatureIds = new ArrayList<>();
            for (UUID creatureId : remainingIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
                if (ownerId.equals(ownerId(gameData, creature))) {
                    ownerCreatureIds.add(creatureId);
                } else {
                    laterCreatureIds.add(creatureId);
                }
            }

            MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary nextContext =
                    new MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary(laterCreatureIds,
                            topIds, bottomIds, sourceCardName);
            playerInputService.beginMultiPermanentChoice(gameData, ownerId, ownerCreatureIds,
                    ownerCreatureIds.size(), nextContext,
                    sourceCardName + " — Choose attacking creatures to put on top of their owners' libraries. "
                            + "The rest go on the bottom.");
            return;
        }

        for (UUID creatureId : topIds.reversed()) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null && permanentRemovalService.removePermanentToLibraryTop(gameData, creature)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(creature.getCard(), " is put on top of its owner's library."));
            }
        }
        for (UUID creatureId : bottomIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null && permanentRemovalService.removePermanentToLibraryBottom(gameData, creature)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(creature.getCard(), " is put on the bottom of its owner's library."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private UUID ownerId(GameData gameData, Permanent permanent) {
        UUID ownerId = permanent.getCard().getOwnerId();
        if (ownerId == null) {
            ownerId = gameData.defaultControllerOf(permanent.getId());
        }
        if (ownerId == null) {
            ownerId = gameData.currentlyResolvingControllerId;
        }
        return ownerId;
    }

    private void handleDestroyCreaturesOpponentControls(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                        MultiPermanentChoiceContext.DestroyCreaturesOpponentControls ctx) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to destroy any creatures."));
        } else {
            for (UUID permId : permanentIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, permId);
                if (creature != null) {
                    destructionSupport.tryDestroyAndLog(gameData, creature, ctx.sourceName(), ctx.cannotBeRegenerated());
                }
            }
        }

        // Resume resolving remaining effects on the same entry (the chooser's "draws up to three cards")
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapChosenPermanents(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                           MultiPermanentChoiceContext.TapChosenPermanents ctx) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to tap any permanents."));
        } else {
            int tapped = 0;
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null && tapUntapSupport.tapPermanent(gameData, perm)) {
                    tapped++;
                }
            }
            gameLogService.append(gameData, GameLog.text(
                    ctx.sourceName() + " taps " + tapped + " permanent(s)."));
            log.info("Game {} - {} taps {} chosen permanent(s)", gameData.id, ctx.sourceName(), tapped);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapPermanentsForAmount(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                              MultiPermanentChoiceContext.TapPermanentsForAmount ctx) {
        int tapped = 0;
        for (UUID permId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permId);
            if (permanent != null && tapUntapSupport.tapPermanent(gameData, permanent)) {
                tapped++;
            }
        }
        gameLogService.append(gameData, GameLog.text(
                ctx.sourceName() + " taps " + tapped + " permanent(s)."));
        log.info("Game {} - {} taps {} chosen permanent(s)", gameData.id, ctx.sourceName(), tapped);

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleUntapChosenPermanents(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                             MultiPermanentChoiceContext.UntapChosenPermanents ctx) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to untap any permanents."));
        } else {
            int untapped = 0;
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    tapUntapSupport.untapPermanent(gameData, perm);
                    untapped++;
                }
            }
            gameLogService.append(gameData, GameLog.text(
                    ctx.sourceName() + " untaps " + untapped + " permanent(s)."));
            log.info("Game {} - {} untaps {} chosen permanent(s)", gameData.id, ctx.sourceName(), untapped);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleUntapPermanentsForAmount(GameData gameData, List<UUID> permanentIds,
                                                MultiPermanentChoiceContext.UntapPermanentsForAmount ctx) {
        int untapped = 0;
        for (UUID permId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permId);
            if (permanent != null && tapUntapSupport.untapPermanent(gameData, permanent)) {
                untapped++;
            }
        }
        gameLogService.append(gameData, GameLog.text(
                ctx.sourceName() + " untaps " + untapped + " permanent(s)."));
        log.info("Game {} - {} untaps {} chosen permanent(s)", gameData.id, ctx.sourceName(), untapped);

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleReturnTargetPermanentsToHand(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to return any permanents."));
        } else {
            List<Card> bouncedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null && permanentRemovalService.removePermanentToHand(gameData, perm)) {
                    bouncedCards.add(perm.getCard());
                }
            }
            if (!bouncedCards.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                gameLogService.append(gameData,
                        appendCards(GameLog.builder(), bouncedCards)
                                .text((bouncedCards.size() == 1 ? " is" : " are")
                                        + " returned to their owners' hands.").build());
                log.info("Game {} - {} returns {} permanents to hand", gameData.id,
                        gameData.playerIdToName.get(playerId), bouncedCards.size());
            }
        }

        // Resume resolving remaining effects on the same ability (e.g. the cycling draw)
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleReturnAnyNumberAndRecordCount(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.ReturnAnyNumberAndRecordCount context) {
        List<Card> bouncedCards = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                    && permanentRemovalService.removePermanentToHand(gameData, permanent)) {
                bouncedCards.add(permanent.getCard());
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        context.resolvingEntry().setEventValue(bouncedCards.size());

        if (bouncedCards.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(playerId) + " chooses not to return any permanents."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder(), bouncedCards)
                            .text((bouncedCards.size() == 1 ? " is" : " are")
                                    + " returned to their owners' hands.").build());
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleRemoveCounterFromChosenPermanents(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.RemoveCounterFromChosenPermanents context) {
        int removedCount = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null
                    || !playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, permanent, context.permanentFilter())) {
                continue;
            }

            int currentCount = permanent.getCounterCount(context.counterType());
            if (currentCount > 0) {
                permanent.setCounterCount(context.counterType(), currentCount - 1);
                removedCount++;
            }
        }
        context.resolvingEntry().setEventValue(removedCount);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(playerId) + " removes " + removedCount
                        + " counter" + (removedCount == 1 ? "" : "s") + "."));

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleReturnNControlledPermanentsToHand(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.ReturnNControlledPermanentsToHand context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending effect resolution entry");
        }
        returnNControlledPermanentsToHandEffectHandler.completeChoice(gameData, permanentIds, context, entry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedSacrifice(GameData gameData, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.ForcedSacrifice context) {
        boolean simultaneousFlow = context.simultaneousFlow()
                || !context.accumulatedSacrificeIds().isEmpty()
                || !context.remainingChoosers().isEmpty();

        if (simultaneousFlow) {
            // "Each player sacrifices" flow — defer actual sacrifice until all players have chosen.
            // Per CR 101.4: all chosen permanents are sacrificed at the same time.
            List<UUID> allIds = new ArrayList<>(context.accumulatedSacrificeIds());
            allIds.addAll(permanentIds);

            if (!context.remainingChoosers().isEmpty()) {
                // More players still need to choose — prompt the next one
                destructionSupport.beginNextForcedSacrificeFromQueue(gameData,
                        context.remainingChoosers(), allIds);
                return;
            }

            // All players have chosen — sacrifice all simultaneously
            destructionSupport.performSimultaneousSacrifice(gameData, allIds);
        } else {
            // Direct forced sacrifice (e.g. Phyrexian Obliterator) — sacrifice immediately
            destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
        }

        if (context.recordSacrificedCount() && gameData.pendingEffectResolutionEntry != null) {
            int sacrificedCount = simultaneousFlow
                    ? context.accumulatedSacrificeIds().size() + permanentIds.size()
                    : permanentIds.size();
            gameData.pendingEffectResolutionEntry.setEventValue(sacrificedCount);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // Follow the same pattern as proliferate completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedSacrificeThenDamageIfSubtype(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.ForcedSacrificeThenDamageIfSubtype context) {
        List<UUID> allIds = new ArrayList<>(context.accumulatedSacrificeIds());
        allIds.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            tappedLandSacrificeDamageIfSubtypeHandler.beginNextChooser(gameData,
                    context.remainingChoosers(), allIds, context.subtype(), context.damageAmount(),
                    context.damageEntry());
            return;
        }

        tappedLandSacrificeDamageIfSubtypeHandler.sacrificeThenDamageIfSubtype(gameData,
                context.damageEntry(), allIds, context.subtype(), context.damageAmount());

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedDestroy(GameData gameData, List<UUID> permanentIds,
                                     MultiPermanentChoiceContext.ForcedDestroy context) {
        // Chosen permanents are destroyed simultaneously (regeneration/indestructible apply).
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.tryDestroyAndLog(gameData, perm, context.sourceName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedReturnToHand(GameData gameData, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.ForcedReturnToHand context) {
        List<Card> bouncedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && permanentRemovalService.removePermanentToHand(gameData, perm)) {
                bouncedCards.add(perm.getCard());
            }
        }
        if (!bouncedCards.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            gameLogService.append(gameData,
                    appendCards(GameLog.builder(), bouncedCards)
                            .text((bouncedCards.size() == 1 ? " is" : " are")
                                    + " returned to their owners' hands.").build());
            log.info("Game {} - {} returns {} permanents to hand", gameData.id,
                    gameData.playerIdToName.get(context.returningPlayerId()), bouncedCards.size());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeLandsSearchLandsToBattlefieldTapped(GameData gameData, UUID playerId,
                                                                    List<UUID> permanentIds) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Search the library for up to that many land cards, put them onto the battlefield tapped.
        if (sacrificed > 0 && !librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            } else {
                List<Card> lands = deck.stream()
                        .filter(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                        .toList();
                if (lands.isEmpty()) {
                    com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                    gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no land cards. Library is shuffled."));
                } else {
                    String prompt = "Search your library for up to " + sacrificed + " land card"
                            + (sacrificed != 1 ? "s" : "")
                            + " and put them onto the battlefield tapped (" + sacrificed + " remaining).";
                    librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId,
                            com.github.laxika.magicalvibes.model.LibrarySearchParams.builder(playerId, new ArrayList<>(lands))
                                    .remainingCount(sacrificed)
                                    .canFailToFind(true)
                                    .destination(com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED)
                                    .build(), prompt, true);
                    // Library search interaction is now active; it resumes effect resolution on completion.
                    return;
                }
            }
        }

        // No search begun — follow standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificePermanentsDrawPerSacrificed(GameData gameData, UUID playerId,
                                                            List<UUID> permanentIds) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (sacrificed > 0) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, sacrificed);
        } else {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " sacrifices no permanents."));
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeAnyNumberAndRecordCount(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount context) {
        int sacrificed = 0;
        int sacrificedPower = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                if (context.recordSacrificedPower()) {
                    sacrificedPower += gameQueryService.getEffectivePower(gameData, permanent);
                }
                destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        context.resolvingEntry().setEventValue(sacrificed);
        if (context.recordSacrificedPower()) {
            context.resolvingEntry().setSacrificedPower(sacrificedPower);
        }

        if (sacrificed == 0) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(playerId) + " sacrifices no permanents."));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /**
     * Mana Seism: the chosen permanents are sacrificed, then the controller adds one mana of the
     * context's color for each permanent actually sacrificed.
     */
    private void handleSacrificePermanentsAddManaPerSacrificed(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificePermanentsAddManaPerSacrificed context) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        String playerName = gameData.playerIdToName.get(playerId);
        if (sacrificed > 0) {
            gameData.playerManaPools.get(playerId).add(context.color(), sacrificed);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " adds " + sacrificed + " " + context.color().getCode() + "."));
            log.info("Game {} - {} adds {} {}", gameData.id, playerName, sacrificed, context.color());
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " sacrifices no permanents."));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificePermanentsOrElse(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificePermanentsOrElse context) {
        int sacrificed = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending effect resolution for optional sacrifice");
        }
        CardEffect branch = sacrificed == context.requiredCount()
                ? context.sacrificedEffect()
                : context.elseEffect();
        entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(branch));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private int totalEffectivePower(GameData gameData, List<UUID> permanentIds) {
        int total = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                total += Math.max(0, gameQueryService.getEffectivePower(gameData, perm));
            }
        }
        return total;
    }

    private int totalPower(GameData gameData, List<UUID> permanentIds) {
        int total = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                total += gameQueryService.getEffectivePower(gameData, perm);
            }
        }
        return total;
    }

    private void handlePowerLimitedCreatureChoice(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice context) {
        powerLimitedCreatureChoiceHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Phyrexian Dreadnought: an empty selection means the controller declined, so the source is
     * sacrificed. Otherwise the chosen creatures are sacrificed (their total power was validated
     * against the threshold before the interaction was cleared) and the source survives.
     */
    private void handleSacrificeCreaturesWithTotalPowerOrSacrificeSource(GameData gameData, UUID playerId,
            List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource context) {
        if (permanentIds.isEmpty()) {
            Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
            if (source != null) {
                destructionSupport.sacrificeAndLog(gameData, source, playerId);
            }
        } else {
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                }
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseFivePermanentsSearchSameName(GameData gameData, UUID playerId,
                                                          List<UUID> permanentIds) {
        List<String> names = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                names.add(perm.getCard().getName());
            }
        }

        if (names.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses no permanents."));
        } else if (librarySearchSupport.startNextSameNamePick(gameData, playerId,
                LibrarySearchFollowUp.sameNamePicks(names, false,
                        com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED))) {
            // A same-name search is now active; it resumes effect resolution on completion.
            return;
        } else if (!librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            // The controller searched but found no matching cards — shuffle once.
            com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " finds no matching cards. Library is shuffled."));
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDestroyRestChoice(GameData gameData, List<UUID> permanentIds,
                                         MultiPermanentChoiceContext.DestroyRestChoice context) {
        destructionSupport.completeDestroyRestChoice(gameData, permanentIds, context);

        // If we're still awaiting input (next player's choice), return
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Destruction is complete — follow standard completion: SBA → may abilities → resume effects
        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseCreatureRestCantBlock(GameData gameData, List<UUID> permanentIds,
                                                   MultiPermanentChoiceContext.ChooseCreatureRestCantBlock context) {
        UUID targetPlayerId = context.targetPlayerId();
        UUID keptId = permanentIds.isEmpty() ? null : permanentIds.getFirst();

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !perm.getId().equals(keptId)) {
                    perm.setCantBlockThisTurn(true);
                    count++;
                }
            }
        }

        if (count > 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text("Other creatures controlled by " + playerName + " can't block this turn."));
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseCreaturesToAttackNextTurn(GameData gameData, List<UUID> permanentIds,
                                                       MultiPermanentChoiceContext.ChooseCreaturesToAttackNextTurn context) {
        UUID targetPlayerId = context.targetPlayerId();
        gameData.chosenAttackersNextTurn.put(targetPlayerId, Set.copyOf(permanentIds));

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " chooses " + permanentIds.size()
                + " creature(s) that must attack during their next turn; other creatures can't attack."));

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleCombatDamageBounce(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.CombatDamageBounce context) {
        UUID targetPlayerId = context.targetPlayerId();

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to return any permanents.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            List<Permanent> targetBattlefield = gameData.playerBattlefields.get(targetPlayerId);
            List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
            List<String> bouncedNames = new ArrayList<>();

            for (UUID permId : permanentIds) {
                Permanent toReturn = null;
                for (Permanent p : targetBattlefield) {
                    if (p.getId().equals(permId)) {
                        toReturn = p;
                        break;
                    }
                }
                if (toReturn != null) {
                    targetBattlefield.remove(toReturn);
                    targetHand.add(toReturn.getCard());
                    bouncedNames.add(toReturn.getCard().getName());
                }
            }

            if (!bouncedNames.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                String logEntry = String.join(", ", bouncedNames) + (bouncedNames.size() == 1 ? " is" : " are") + " returned to " + gameData.playerIdToName.get(targetPlayerId) + "'s hand.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.advanceStep(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleAwakeningCounterPlacement(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to put awakening counters on any lands.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            List<Card> awakenedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.setCounterCount(CounterType.AWAKENING, perm.getCounterCount(CounterType.AWAKENING) + 1);
                    awakenedCards.add(perm.getCard());
                }
            }

            if (!awakenedCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder(), awakenedCards)
                                .text((awakenedCards.size() == 1 ? " receives" : " receive")
                                        + " an awakening counter and "
                                        + (awakenedCards.size() == 1 ? "becomes an" : "become")
                                        + " 8/8 green Elemental creature"
                                        + (awakenedCards.size() == 1 ? "." : "s."))
                                .build());
                log.info("Game {} - Awakening counters placed on {} lands", gameData.id, awakenedCards.size());
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.advanceStep(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleAimCounterPlacement(GameData gameData, List<UUID> permanentIds) {
        if (gameData.pendingEffectResolutionEntry != null) {
            permanentCounterSupport.placeCountersOnPermanents(gameData,
                    gameData.pendingEffectResolutionEntry, permanentIds, CounterType.AIM);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleOwnPermanentCounterPlacement(GameData gameData, List<UUID> permanentIds,
                                                    MultiPermanentChoiceContext.OwnPermanentCounterPlacement context) {
        CounterType counterType = context.counterType();
        int count = context.count();

        if (!permanentIds.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                permanentCounterSupport.placeCounterOnPermanent(gameData,
                        gameData.pendingEffectResolutionEntry, target, counterType, count);
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleOwnPermanentCounterPlacementByPlayer(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.OwnPermanentCounterPlacementByPlayer context) {
        if (!permanentIds.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                StackEntry placementEntry = new StackEntry(gameData.pendingEffectResolutionEntry);
                placementEntry.setControllerId(context.placingPlayerId());
                permanentCounterSupport.placeCounterOnPermanent(gameData, placementEntry, target,
                        context.counterType(), context.count());
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleOpponentCreatureCounterPlacement(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.OpponentCreatureCounterPlacement context) {
        Permanent target = permanentIds.isEmpty() ? null
                : gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
        if (target != null && gameQueryService.isCreature(gameData, target)
                && !gameQueryService.cantHaveCounters(gameData, target)
                && (context.counterType() != CounterType.PLUS_ONE_PLUS_ONE
                || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target))
                && (context.counterType() != CounterType.MINUS_ONE_MINUS_ONE
                || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target))
                && gameData.pendingEffectResolutionEntry != null) {
            StackEntry placementEntry = new StackEntry(gameData.pendingEffectResolutionEntry);
            placementEntry.setControllerId(context.placingPlayerId());
            permanentCounterSupport.placeCounterOnPermanent(gameData, placementEntry, target,
                    context.counterType(), 1);
        }

        int remainingCount = context.remainingCount() - 1;
        if (remainingCount > 0) {
            UUID opponentId = gameQueryService.getOpponentId(gameData, context.placingPlayerId());
            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, opponentId,
                    permanent -> !gameQueryService.cantHaveCounters(gameData, permanent)
                            && (context.counterType() != CounterType.PLUS_ONE_PLUS_ONE
                            || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent))
                            && (context.counterType() != CounterType.MINUS_ONE_MINUS_ONE
                            || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)));
            if (!candidates.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, context.placingPlayerId(), candidates, 1,
                        new MultiPermanentChoiceContext.OpponentCreatureCounterPlacement(
                                context.counterType(), remainingCount, context.placingPlayerId()),
                        "Choose a creature to put a counter on.");
                return;
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleOwnPermanentCounterPlacementWithChosenReference(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.OwnPermanentCounterPlacementWithChosenReference context) {
        if (!permanentIds.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                gameData.pendingEffectResolutionEntry.setChosenPermanentId(target.getId());
                int placed = permanentCounterSupport.placeCounterOnPermanent(gameData,
                        gameData.pendingEffectResolutionEntry, target, context.counterType(), context.count());
                if (context.recordPlacement() && placed > 0
                        && !gameData.pendingEffectResolutionEntry.getCounteredPermanentIdsThisResolution()
                        .contains(target.getId())) {
                    gameData.pendingEffectResolutionEntry.getCounteredPermanentIdsThisResolution()
                            .add(target.getId());
                    gameData.pendingEffectResolutionEntry.setEventValue(
                            gameData.pendingEffectResolutionEntry.getEventValue() + 1);
                }
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleProliferate(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                   MultiPermanentChoiceContext.Proliferate context) {
        int remainingProliferates = context.remainingCount() - 1;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to proliferate any permanents or players.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            List<Card> proliferatedCards = new ArrayList<>();
            List<String> proliferatedPlayers = new ArrayList<>();
            Map<UUID, Integer> loyaltyCountersPlacedByController = new HashMap<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    if (!gameQueryService.cantHaveCounters(gameData, perm)) {
                        proliferatePermanent(gameData, playerId, perm, loyaltyCountersPlacedByController);
                    }
                    proliferatedCards.add(perm.getCard());
                } else if (gameData.playerIds.contains(permId)
                        && gameData.playerPoisonCounters.getOrDefault(permId, 0) > 0) {
                    int placed = gameQueryService.applyPoisonCounterReplacement(gameData, permId, 1);
                    placed = gameQueryService.replacePoisonCounters(gameData, permId, placed);
                    if (placed > 0) {
                        int currentPoison = gameData.playerPoisonCounters.getOrDefault(permId, 0);
                        gameData.playerPoisonCounters.put(permId, currentPoison + placed);
                        triggerCollectionService.checkYouPutCountersTriggers(gameData, playerId, placed);
                        proliferatedPlayers.add(gameData.playerIdToName.get(permId));
                    }
                }
            }

            if (!proliferatedCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text("Proliferate adds counters to "), proliferatedCards)
                                .text(".").build());
                log.info("Game {} - Proliferated {} permanents", gameData.id, proliferatedCards.size());
            }
            if (!proliferatedPlayers.isEmpty()) {
                gameLogService.append(gameData, GameLog.text("Proliferate adds poison counters to "
                        + String.join(", ", proliferatedPlayers) + "."));
                log.info("Game {} - Proliferated {} players", gameData.id, proliferatedPlayers.size());
            }
            loyaltyCountersPlacedByController.forEach((controllerId, count) ->
                    permanentCounterSupport.fireLoyaltyCountersPutOnControlledPlaneswalkersTriggers(
                            gameData, controllerId, count));
        }

        // More proliferates remaining (e.g. "proliferate, then proliferate again")
        // Per MTG Rule 704.3, SBA are not checked during ability resolution,
        // so defer SBA until all proliferates are done.
        if (remainingProliferates > 0) {
            List<UUID> eligiblePermanentIds = new ArrayList<>();
            gameData.forEachPermanent((pid, p) -> {
                if (p.getCounters().values().stream().anyMatch(count -> count > 0)) {
                    eligiblePermanentIds.add(p.getId());
                }
            });
            List<UUID> eligiblePlayerIds = new ArrayList<>();
            for (UUID candidatePlayerId : gameData.playerIds) {
                if (gameData.playerPoisonCounters.getOrDefault(candidatePlayerId, 0) > 0) {
                    eligiblePlayerIds.add(candidatePlayerId);
                }
            }
            if (eligiblePermanentIds.isEmpty() && eligiblePlayerIds.isEmpty()) {
                String logEntry = "Proliferate: no permanents or players with counters to choose.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            } else {
                MultiPermanentChoiceContext.Proliferate nextContext =
                        new MultiPermanentChoiceContext.Proliferate(remainingProliferates);
                int maxCount = eligiblePermanentIds.size() + eligiblePlayerIds.size();
                if (eligiblePlayerIds.isEmpty()) {
                    playerInputService.beginMultiPermanentChoice(gameData, playerId, eligiblePermanentIds,
                            maxCount, nextContext, "Proliferate: Choose permanents to add counters to.");
                } else {
                    playerInputService.beginMultiPermanentOrPlayerChoice(gameData, playerId,
                            eligiblePermanentIds, eligiblePlayerIds, maxCount, nextContext,
                            "Proliferate: Choose permanents and/or players to add counters to.");
                }
                return;
            }
        }

        // All proliferates done — now check SBA
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void proliferatePermanent(GameData gameData, UUID playerId, Permanent permanent,
                                      Map<UUID, Integer> loyaltyCountersPlacedByController) {
        int totalPlaced = 0;
        for (var counter : new ArrayList<>(permanent.getCounters().entrySet())) {
            CounterType counterType = counter.getKey();
            if (counter.getValue() <= 0
                    || (counterType == CounterType.PLUS_ONE_PLUS_ONE
                    && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent))
                    || (counterType == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))) {
                continue;
            }
            int previousCount = permanent.getCounterCount(counterType);
            int placed = gameQueryService.replaceCounters(gameData, permanent, counterType, 1);
            if (placed <= 0) {
                continue;
            }
            permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) + placed);
            permanentCounterSupport.notifySelfCountersPlaced(
                    gameData, null, permanent, counterType, previousCount, placed);
            totalPlaced += placed;
            if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, permanent);
            } else if (counterType == CounterType.MINUS_ONE_MINUS_ONE) {
                permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                        gameData, permanent, placed, playerId);
            } else if (counterType == CounterType.LOYALTY
                    && gameQueryService.isPlaneswalker(gameData, permanent)) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (controllerId != null) {
                    loyaltyCountersPlacedByController.merge(controllerId, placed, Integer::sum);
                }
            }
        }
        if (totalPlaced > 0) {
            triggerCollectionService.checkYouPutCountersTriggers(gameData, playerId, totalPlaced);
        }
    }

    private void handleTapSubtypeBoost(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.TapSubtypeBoost context) {
        UUID sourcePermanentId = context.sourcePermanentId();

        int count = permanentIds.size();

        if (count == 0) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to tap any Myr.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            // Tap the chosen permanents
            List<Card> tappedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.tap();
                    triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                    tappedCards.add(perm.getCard());
                }
            }

            if (!tappedCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId) + " taps "
                                + tappedCards.size() + " Myr: "), tappedCards).text(".").build());
                log.info("Game {} - {} taps {} Myr for attack trigger", gameData.id,
                        gameData.playerIdToName.get(playerId), tappedCards.size());
            }

            // Boost source permanent +X/+0 (only if still on battlefield)
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            Card sourceCard = sourcePermanent != null ? sourcePermanent.getCard() : null;
            String sourceName;
            if (sourcePermanent != null) {
                sourcePermanent.setPowerModifier(sourcePermanent.getPowerModifier() + count);
                sourceName = sourcePermanent.getCard().getName();
                gameLogService.append(gameData,
                        GameLog.cardThen(sourceCard, " gets +" + count + "/+0 until end of turn."));
                log.info("Game {} - {} gets +{}/+0", gameData.id, sourceName, count);
            } else {
                sourceName = "Myr Battlesphere";
                log.info("Game {} - Source permanent no longer on battlefield, skipping boost", gameData.id);
            }

            // Deal X damage to the defending player (happens even if source left battlefield per ruling)
            UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, playerId);
            String defenderName = gameData.playerIdToName.get(defendingPlayerId);

            // Check source damage prevention
            Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(defendingPlayerId);
            boolean sourcePrevented = preventedSources != null && preventedSources.contains(sourcePermanentId);

            if (sourcePrevented) {
                gameLogService.append(gameData, appendCardOrText(GameLog.builder(), sourceCard, sourceName)
                        .text("'s damage to " + defenderName + " is prevented.").build());
            } else {
                // Apply damage multiplier (GlobalDamageMultiplyingEffect)
                int damage = count;
                damage += gameQueryService.getAdditionalDamageToOpponentsBonus(
                        gameData, playerId, sourceCard, sourcePermanent, defendingPlayerId);
                final int[] multiplier = {1};
                gameData.forEachPermanent((pid, p) -> {
                    for (CardEffect e : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (e instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                            multiplier[0] *= multiplyingEffect.damageMultiplierFactor();
                        }
                    }
                });
                damage *= multiplier[0];

                // Apply global prevention shield
                if (gameData.globalDamagePreventionShield > 0 && damage > 0) {
                    int prevented = Math.min(gameData.globalDamagePreventionShield, damage);
                    gameData.globalDamagePreventionShield -= prevented;
                    damage -= prevented;
                }

                damage = damagePreventionService.applyDamagePreventionLifeGainShield(
                        gameData, defendingPlayerId, damage);

                // Apply player prevention shield
                int shield = gameData.playerDamagePreventionShields.getOrDefault(defendingPlayerId, 0);
                if (shield > 0 && damage > 0) {
                    int prevented = Math.min(shield, damage);
                    gameData.playerDamagePreventionShields.put(defendingPlayerId, shield - prevented);
                    damage -= prevented;
                }

                if (damage > 0) {
                    boolean hasInfect = sourcePermanent != null
                            && gameQueryService.hasKeyword(gameData, sourcePermanent, Keyword.INFECT);
                    boolean treatAsInfect = hasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, defendingPlayerId);
                    if (treatAsInfect) {
                        lifeSupport.applyPoisonCounters(gameData, defendingPlayerId, damage,
                                sourceName, playerId);
                    } else if (!gameQueryService.canPlayerLifeChange(gameData, defendingPlayerId)) {
                        gameLogService.append(gameData, GameLog.text(defenderName + "'s life total can't change."));
                    } else {
                        int lifeLoss = damage
                                * gameQueryService.opponentLifeLossMultiplier(gameData, defendingPlayerId);
                        int currentLife = gameData.getLife(defendingPlayerId);
                        gameData.playerLifeTotals.put(defendingPlayerId, currentLife - lifeLoss);
                        gameLogService.append(gameData, appendCardOrText(GameLog.builder(), sourceCard, sourceName)
                                .text(" deals " + damage + " damage to " + defenderName + ".").build());
                    }
                    gameData.recordDamageToPlayer(defendingPlayerId, damage,
                            gameQueryService.isArtifact(gameData, sourcePermanent) ? damage : 0);
                    triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, defendingPlayerId, damage);
                }
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapAnyNumberBoostSelf(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                              MultiPermanentChoiceContext.TapAnyNumberBoostSelf context) {
        List<Card> tappedCards = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && !permanent.isTapped()) {
                permanent.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
                tappedCards.add(permanent.getCard());
            }
        }

        int tappedCount = tappedCards.size();
        if (tappedCount == 0) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(playerId) + " chooses not to tap any permanents."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId)
                            + " taps " + tappedCount + " permanent" + (tappedCount == 1 ? "" : "s") + ": "),
                            tappedCards).text(".").build());
            Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
            if (source != null) {
                int powerBoost = context.powerPerPermanent() * tappedCount;
                int toughnessBoost = context.toughnessPerPermanent() * tappedCount;
                source.setPowerModifier(source.getPowerModifier() + powerBoost);
                source.setToughnessModifier(source.getToughnessModifier() + toughnessBoost);
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        " gets +" + powerBoost + "/+" + toughnessBoost + " until end of turn."));
                log.info("Game {} - {} gets +{}/+{}", gameData.id, source.getCard().getName(),
                        powerBoost, toughnessBoost);
            }
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapCreaturesGainLife(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.TapCreaturesGainLife context) {
        List<Card> tappedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !perm.isTapped()) {
                perm.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                tappedCards.add(perm.getCard());
            }
        }

        int tappedCount = tappedCards.size();
        if (tappedCount == 0) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " taps no creatures."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId)
                            + " taps " + tappedCount + " creature" + (tappedCount == 1 ? "" : "s") + ": "), tappedCards)
                            .text(".").build());
            lifeSupport.applyGainLife(gameData, playerId, context.lifePerCreature() * tappedCount);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapCreaturesBoostSelf(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                             MultiPermanentChoiceContext.TapCreaturesBoostSelf context) {
        int tappedCount = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && gameQueryService.isCreature(gameData, permanent)
                    && tapUntapSupport.tapPermanent(gameData, permanent)) {
                tappedCount++;
            }
        }

        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source != null && tappedCount > 0) {
            source.setPowerModifier(source.getPowerModifier() + tappedCount);
            source.setToughnessModifier(source.getToughnessModifier() + tappedCount);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    " gets +" + tappedCount + "/+" + tappedCount + " until end of turn."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapOtherCreaturesForUnblockable(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.TapOtherCreaturesForUnblockable context) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to tap other creatures."));
        } else {
            int tappedCount = 0;
            for (UUID permanentId : permanentIds) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (permanent != null && gameQueryService.isCreature(gameData, permanent)
                        && tapUntapSupport.tapPermanent(gameData, permanent)) {
                    tappedCount++;
                }
            }
            if (tappedCount == context.requiredCount()) {
                makeCreatureUnblockableEffectHandler.makeUnblockable(
                        gameData, gameQueryService.findPermanentById(gameData, context.sourcePermanentId()));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapCreaturesCreateTokens(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                MultiPermanentChoiceContext.TapCreaturesCreateTokens context) {
        List<Card> tappedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !perm.isTapped()) {
                perm.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                tappedCards.add(perm.getCard());
            }
        }

        int tappedCount = tappedCards.size();
        if (tappedCount == 0) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " taps no creatures."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId)
                            + " taps " + tappedCount + " creature" + (tappedCount == 1 ? "" : "s") + ": "), tappedCards)
                            .text(".").build());
            permanentControlSupport.applyCreateToken(gameData, playerId, context.tokenTemplate(), tappedCount,
                    context.sourceSetCode());
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapPermanentsDrawPerTapped(GameData gameData, UUID playerId,
                                                  List<UUID> permanentIds) {
        int tapped = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && tapUntapSupport.tapPermanent(gameData, permanent)) {
                tapped++;
            }
        }

        if (tapped > 0) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, tapped);
        } else {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " taps no permanents."));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapCreaturesThenQueueReflexiveAbility(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.TapCreaturesThenQueueReflexiveAbility context) {
        int tapped = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && tapUntapSupport.tapPermanent(gameData, permanent)) {
                tapped++;
            }
        }

        context.resolvingEntry().setEventValue(tapped);
        context.resolvingEntry().setXValue(tapped);
        if (tapped > 0) {
            queueReflexiveAbilityEffectHandler.resolve(gameData, context.resolvingEntry(),
                    new com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect(
                            context.reflexiveEffect()));
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private void handleTapPermanentsAndPutCounters(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                   MultiPermanentChoiceContext.TapPermanentsAndPutCounters context) {
        List<Permanent> tappedPermanents = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && !permanent.isTapped()) {
                permanent.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
                tappedPermanents.add(permanent);
            }
        }

        if (tappedPermanents.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " taps no permanents."));
        } else {
            List<Card> tappedCards = tappedPermanents.stream().map(Permanent::getCard).toList();
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId)
                            + " taps " + tappedPermanents.size() + " permanent"
                            + (tappedPermanents.size() == 1 ? "" : "s") + ": "), tappedCards)
                            .text(".").build());
            StackEntry entry = gameData.pendingEffectResolutionEntry;
            for (Permanent permanent : tappedPermanents) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent,
                        context.counterType(), 1);
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDevourSacrifice(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.DevourSacrifice context) {
        Permanent entering = gameQueryService.findPermanentById(gameData, context.enteringPermanentId());

        int devoured = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                if (entering != null) {
                    entering.recordDevouredCreature(perm.getCard());
                }
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                devoured++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (entering != null && devoured > 0) {
            if (!gameQueryService.cantHaveCounters(gameData, entering)) {
                int added = context.multiplier() * devoured;
                added = gameQueryService.doublePlusOnePlusOneCounters(gameData, entering, playerId, added);
                entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + added);
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, entering, context.controllerId());
                gameLogService.append(gameData, GameLog.cardThen(context.card(),
                        " devours " + devoured + " creature" + (devoured == 1 ? "" : "s")
                                + " and enters with " + added + " +1/+1 counter" + (added == 1 ? "" : "s") + "."));
            }
        }

        // Resume the entry: run ETB triggers now that the devour counters/count are set.
        battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                context.targetId(), context.wasCastFromHand(), context.etbMode(), context.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    /**
     * Dracoplasm: sacrifice the chosen creatures and set the entering permanent's base power and
     * toughness to their totals, read from their last known battlefield values before they leave.
     * The stamp is a durable layer-7b base-P/T override, so counters and boosts apply on top of it.
     */
    private void handleSacrificeCreaturesSetEnteringPowerToughness(
            GameData gameData, UUID playerId, List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificeCreaturesSetEnteringPowerToughness context) {
        Permanent entering = gameQueryService.findPermanentById(gameData, context.enteringPermanentId());

        int totalPower = 0;
        int totalToughness = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                totalPower += gameQueryService.getEffectivePower(gameData, perm);
                totalToughness += gameQueryService.getEffectiveToughness(gameData, perm);
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (entering != null) {
            entering.setBasePowerOverriddenPermanently(true);
            entering.setPermanentBasePowerOverride(totalPower);
            entering.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
            entering.setBaseToughnessOverriddenPermanently(true);
            entering.setPermanentBaseToughnessOverride(totalToughness);
            entering.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());
            gameLogService.append(gameData, GameLog.cardThen(context.card(),
                    " becomes " + totalPower + "/" + totalToughness + "."));
        }

        battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                context.targetId(), context.wasCastFromHand(), context.etbMode(), context.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private void handleSacrificeAsEntersForCounters(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                    MultiPermanentChoiceContext.SacrificeAsEntersForCounters context) {
        Permanent entering = gameQueryService.findPermanentById(gameData, context.enteringPermanentId());

        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (entering != null && sacrificed > 0
                && !gameQueryService.cantHaveCounters(gameData, entering)) {
            int added = context.countersPerPermanent() * sacrificed;
            added = gameQueryService.doublePlusOnePlusOneCounters(gameData, entering, playerId, added);
            entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + added);
            permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                    gameData, entering, context.controllerId());
            gameLogService.append(gameData, GameLog.cardThen(context.card(),
                    " enters with " + added + " +1/+1 counter" + (added == 1 ? "" : "s") + "."));
        }

        // Resume the entry: run ETB triggers now that the counters are set.
        battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                context.targetId(), context.wasCastFromHand(), context.etbMode(), context.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private void handleSacrificePermanentsToEnter(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                  MultiPermanentChoiceContext.SacrificePermanentsToEnter context) {
        if (!permanentIds.isEmpty()) {
            for (UUID permId : permanentIds) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, permId);
                destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
            }
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        battlefieldEntryService.completeSacrificePermanentsToEnter(
                gameData, context.controllerId(), context.enteringPermanent(), !permanentIds.isEmpty());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private void handlePayManaPerCreatureUntap(GameData gameData, List<UUID> permanentIds,
                                               MultiPermanentChoiceContext.PayManaPerCreatureUntap context) {
        UUID actingPlayerId = context.actingPlayerId();
        String playerName = gameData.playerIdToName.get(actingPlayerId);

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " untaps no creatures."));
        } else {
            com.github.laxika.magicalvibes.model.ManaCost cost =
                    new com.github.laxika.magicalvibes.model.ManaCost(context.manaCost());
            com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(actingPlayerId);
            boolean canPay = pool != null;
            com.github.laxika.magicalvibes.model.ManaPool remaining = canPay
                    ? new com.github.laxika.magicalvibes.model.ManaPool(pool) : null;
            for (int i = 0; canPay && i < permanentIds.size(); i++) {
                canPay = cost.canPay(remaining);
                if (canPay) {
                    cost.pay(remaining);
                }
            }
            if (canPay) {
                for (int i = 0; i < permanentIds.size(); i++) {
                    cost.pay(pool);
                }
                List<Card> untappedCards = new ArrayList<>();
                for (UUID permId : permanentIds) {
                    Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                    if (perm != null && tapUntapSupport.untapPermanent(gameData, perm)) {
                        untappedCards.add(perm.getCard());
                    }
                }
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(playerName + " pays " + context.manaCost()
                                        + " per creature and untaps "),
                                untappedCards).text(".").build());
                log.info("Game {} - {} pays {} per creature to untap {} creature(s)", gameData.id, playerName,
                        context.manaCost(),
                        untappedCards.size());
            } else {
                gameLogService.append(gameData, GameLog.text(playerName
                        + " can't pay " + context.manaCost() + " per creature — untaps no creatures."));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleKillingWaveKeep(GameData gameData, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.KillingWaveKeep context) {
        killingWaveEffectHandler.completeKeepChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleFadeAwayKeep(GameData gameData, List<UUID> permanentIds,
                                    MultiPermanentChoiceContext.FadeAwayKeep context) {
        fadeAwayEffectHandler.completeKeepChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleFadeAwaySacrifice(GameData gameData, List<UUID> permanentIds,
                                         MultiPermanentChoiceContext.FadeAwaySacrifice context) {
        fadeAwayEffectHandler.completeSacrificeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleKeepOneOfEachTypeChoice(GameData gameData, List<UUID> permanentIds,
                                               MultiPermanentChoiceContext.KeepOneOfEachTypeChoice context) {
        keepOneOfEachTypeHandler.completeKeepChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleWinnowingChoice(GameData gameData, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.WinnowingChoice context) {
        winnowingHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleEachPlayerReturnsCreature(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerReturnsCreature context) {
        eachPlayerReturnsCreatureToHandHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDestroyUpToOneNonbasicLandPerPlayerChoice(GameData gameData,
            List<UUID> permanentIds,
            MultiPermanentChoiceContext.DestroyUpToOneNonbasicLandPerPlayerChoice context) {
        destroyUpToOneNonbasicLandPerPlayerThenSearchHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleEachPlayerSacrificeOneOfEachTypeChoice(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerSacrificeOneOfEachTypeChoice context) {
        eachPlayerSacrificesOneOfEachTypeHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleEachPlayerChoosesLandOfEachBasicTypeChoice(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeChoice context) {
        eachPlayerChoosesLandOfEachBasicTypeHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleEachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice context) {
        eachPlayerChoosesLandOfEachBasicTypeThenReturnToHandHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseLandOfEachBasicTypeThenDestroyChoice(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice context) {
        chooseLandOfEachBasicTypeThenDestroyHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleEachPlayerChoosesLandsThenDestroyRestChoice(
            GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerChoosesLandsThenDestroyRestChoice context) {
        eachPlayerChoosesLandsThenDestroyRestHandler.completeChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleRaidingPartyTapChoice(GameData gameData, List<UUID> permanentIds,
                                             MultiPermanentChoiceContext.RaidingPartyTapChoice context) {
        raidingPartyEffectHandler.completeTapChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleRaidingPartyPlainsChoice(GameData gameData, List<UUID> permanentIds,
                                                MultiPermanentChoiceContext.RaidingPartyPlainsChoice context) {
        raidingPartyEffectHandler.completePlainsChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleExileTetraviteTokensPutCountersOnSource(GameData gameData, List<UUID> permanentIds,
                                                               MultiPermanentChoiceContext.ExileTetraviteTokensPutCountersOnSource context) {
        UUID sourceId = context.sourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        String sourceName = source != null ? source.getCard().getName() : "Tetravus";
        Set<UUID> created = gameData.sourceCreatedTokens.get(sourceId);

        int exiled = 0;
        for (UUID permId : permanentIds) {
            Permanent token = gameQueryService.findPermanentById(gameData, permId);
            if (token != null) {
                permanentRemovalService.removePermanentToExile(gameData, token);
                if (created != null) {
                    created.remove(permId);
                }
                exiled++;
            }
        }

        if (exiled > 0) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            GameLog.Builder tetraviteLog = GameLog.builder().text(
                    exiled + " token" + (exiled == 1 ? "" : "s") + " created with ");
            gameLogService.append(gameData,
                    appendCardOrText(tetraviteLog, source != null ? source.getCard() : null, sourceName)
                            .text((exiled == 1 ? " is" : " are") + " exiled.").build());
            // "Put that many +1/+1 counters on this creature" — only if the source is still around.
            if (source != null) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, null, source, exiled);
            }
            log.info("Game {} - {} tokens created with {} exiled to add +1/+1 counters",
                    gameData.id, exiled, sourceName);
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleStaticOrbUntap(GameData gameData, List<UUID> permanentIds,
                                      MultiPermanentChoiceContext.StaticOrbUntap context) {
        UUID activePlayerId = context.activePlayerId();

        List<Card> untappedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                untappedCards.add(perm.getCard());
            }
        }
        String playerName = gameData.playerIdToName.get(activePlayerId);
        if (untappedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " untaps no permanents (untap lock)."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(playerName + " untaps "), untappedCards)
                            .text(" (untap lock).").build());
        }

        // Resume the untap step: of the permanents matching the lock's filter only the chosen ones
        // untap; permanents the filter excludes untap normally, and the rest of the untap-step
        // bookkeeping and turn advance proceed as normal.
        turnProgressionService.resumeStaticOrbUntap(gameData, activePlayerId, new HashSet<>(permanentIds),
                context.filter());
    }

    private void handleCapriciousEfreetOpponentTargets(GameData gameData, List<UUID> permanentIds) {
        PendingCapriciousEfreetState state = gameData.pollPendingInteraction(PendingCapriciousEfreetState.class);

        // Combine own target + opponent targets
        List<UUID> allTargets = new ArrayList<>();
        allTargets.add(state.ownTargetId());
        allTargets.addAll(permanentIds);

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                state.sourceCard(),
                state.controllerId(),
                state.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyOneOfTargetsAtRandomEffect())),
                0,
                allTargets
        );
        gameData.stack.add(entry);

        List<String> targetNames = new ArrayList<>();
        GameLog.Builder targetsLog = GameLog.builder().card(state.sourceCard()).text("'s ability targets ");
        for (int i = 0; i < allTargets.size(); i++) {
            UUID targetId = allTargets.get(i);
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            targetNames.add(target != null ? target.getCard().getName() : targetId.toString());
            if (i > 0) {
                targetsLog.text(", ");
            }
            appendCardOrText(targetsLog, target != null ? target.getCard() : null, targetId.toString());
        }
        targetsLog.text(".");
        gameLogService.append(gameData, targetsLog.build());
        log.info("Game {} - {} upkeep trigger targets: {}", gameData.id, state.sourceCard().getName(), targetNames);

        // Continue processing: more Efreet triggers → may abilities → priority
        if (gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            turnProgressionService.processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            turnProgressionService.processNextPucasMischiefTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handlePileSeparation(GameData gameData, List<UUID> permanentIds) {
        PendingPileSeparation state = gameData.peekPendingInteraction(PendingPileSeparation.class);
        if (state.disposition() == CardPileDisposition.ATTACKERS) {
            fightOrFlightSupport.completePileSeparationStep1(gameData, permanentIds);
        } else if (state.disposition() == CardPileDisposition.BLOCKERS) {
            standOrFallSupport.completePileSeparationStep1(gameData, permanentIds);
        } else {
            destructionSupport.completePileSeparationStep1(gameData, permanentIds);
        }
    }

    /** Appends {@code cards} as comma-separated card segments (each hoverable) to {@code builder}. */
    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }

    /** Appends a hoverable card segment when {@code card} is known, otherwise falls back to plain text. */
    private static GameLog.Builder appendCardOrText(GameLog.Builder builder, Card card, String fallbackText) {
        if (card != null) {
            builder.card(card);
        } else {
            builder.text(fallbackText);
        }
        return builder;
    }
}
