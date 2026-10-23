package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.CelestialJudgmentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachCurseToOpponentAndDrawEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachSelectedAurasToLegalTargetsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AllureOfTheUnknownEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimalMagnetismEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.BendOrBreakEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DawnbreakReclaimerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DemonstrateEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokenCopyOfChosenCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentChoosesCreatureToTapAndGoadEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EarthbendTargetLandThenFightEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.GrantKeywordToChosenCreatureUntilEndOfTurnEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.GuidedPassageEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RiskyMoveEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MemoriesReturningEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.BoostEquippedCreatureUntilEndOfTurnEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExploreEachControlledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChangeTargetOfTargetSpellToSourceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToChosenCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveCounterFromControlledCreatureThenDrawEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TurnOwnCreatureFaceUpEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetOpponentChoosesPlayerForRestrictionEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MurmursFromBeyondEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveCounterFromChosenOwnPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveAllMireCountersFromChosenLandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SuspectChosenOtherCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnAurasFromGraveyardAttachedToCreaturesEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RedHerringExchangeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RingTemptsYouEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TemptTheRingEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.InfernalOfferingEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.OpponentChoosesCardFromGraveyardToHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ToymakersTrapEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ZndrsplatsJudgmentEffectHandler;
import com.github.laxika.magicalvibes.service.battlefield.DeathReplacementChoicePending;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Dispatcher for permanent choice inputs. Validates the incoming choice
 * and delegates to the appropriate thematic handler service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceHandlerService {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.SplitCardCastSupport splitCardCastSupport;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.trigger.StackedModalTriggerService stackedModalTriggerService;

    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.PermanentAuctionService permanentAuctionService;

    private final LibraryChoiceHandlerService libraryChoiceHandlerService;

    private final PermanentChoiceTriggerHandlerService triggerHandler;
    private final PermanentChoiceSpellHandlerService spellHandler;
    private final PermanentChoiceBattlefieldHandlerService battlefieldHandler;
    private final AbilityActivationService abilityActivationService;
    private final BendOrBreakEffectHandler bendOrBreakEffectHandler;
    private final OpponentChoosesCardFromGraveyardToHandEffectHandler opponentChoosesCardHandler;
    private final DawnbreakReclaimerEffectHandler dawnbreakReclaimerEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReturnPermanentsToHandSimultaneouslyEffectHandler
            returnPermanentsToHandSimultaneouslyEffectHandler;
    private final AllureOfTheUnknownEffectHandler allureOfTheUnknownEffectHandler;
    private final EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffectHandler dispersalEffectHandler;
    private final GuidedPassageEffectHandler guidedPassageEffectHandler;
    private final GrantKeywordToChosenCreatureUntilEndOfTurnEffectHandler grantChosenCreatureKeywordEffectHandler;
    private final PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffectHandler
            kathrilEffectHandler;
    private final EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffectHandler
            gildedAmbusherEffectHandler;
    private final SuspectChosenOtherCreatureEffectHandler suspectChosenOtherCreatureEffectHandler;
    private final ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffectHandler
            contractualSafeguardEffectHandler;
    private final ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffectHandler
            avenCourierEffectHandler;
    private final MurmursFromBeyondEffectHandler murmursFromBeyondEffectHandler;
    private final AnimalMagnetismEffectHandler animalMagnetismEffectHandler;
    private final RiskyMoveEffectHandler riskyMoveEffectHandler;
    private final MemoriesReturningEffectHandler memoriesReturningEffectHandler;
    private final TurnOwnCreatureFaceUpEffectHandler turnOwnCreatureFaceUpEffectHandler;
    private final RemoveCounterFromControlledCreatureThenDrawEffectHandler
            removeCounterFromControlledCreatureThenDrawEffectHandler;
    private final DealDamageToChosenCreatureEffectHandler dealDamageToChosenCreatureEffectHandler;
    private final ChangeTargetOfTargetSpellToSourceEffectHandler changeTargetOfTargetSpellToSourceEffectHandler;
    private final ExploreEachControlledCreatureEffectHandler exploreEachControlledCreatureEffectHandler;
    private final BoostEquippedCreatureUntilEndOfTurnEffectHandler boostEquippedCreatureUntilEndOfTurnEffectHandler;
    private final InputCompletionService inputCompletionService;
    private final EarthbendTargetLandThenFightEffectHandler earthbendThenFightHandler;
    private final ReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler
            returnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler;
    private final RemoveCounterFromChosenOwnPermanentEffectHandler removeCounterFromChosenOwnPermanentEffectHandler;
    private final RemoveAllMireCountersFromChosenLandEffectHandler removeAllMireCountersFromChosenLandEffectHandler;
    private final ReturnAurasFromGraveyardAttachedToCreaturesEffectHandler
            returnAurasFromGraveyardAttachedToCreaturesEffectHandler;
    private final RedHerringExchangeEffectHandler redHerringExchangeEffectHandler;
    private final RingTemptsYouEffectHandler ringTemptsYouEffectHandler;
    private final TemptTheRingEffectHandler temptTheRingEffectHandler;
    private final ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffectHandler
            conjureDuplicateOfChosenCombatDamageDealerIntoHandEffectHandler;
    private final ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffectHandler gutmornEffectHandler;
    private final SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffectHandler descendantsFuryEffectHandler;
    private final InfernalOfferingEffectHandler infernalOfferingEffectHandler;
    private final MayEffectHandlerRegistry mayEffectHandlerRegistry;
    private final ToymakersTrapEffectHandler toymakersTrapEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.InventoryManagementEffectHandler
            inventoryManagementEffectHandler;
    private final CreateTokenCopyOfChosenCreatureEffectHandler esixCreatureCopyHandler;
    private final ZndrsplatsJudgmentEffectHandler zndrsplatsJudgmentEffectHandler;
    private final TargetOpponentChoosesPlayerForRestrictionEffectHandler
            targetOpponentChoosesPlayerForRestrictionEffectHandler;
    private final CelestialJudgmentEffectHandler celestialJudgmentEffectHandler;
    private final AttachCurseToOpponentAndDrawEffectHandler attachCurseToOpponentAndDrawEffectHandler;
    private final AttachSelectedAurasToLegalTargetsEffectHandler attachSelectedAurasHandler;
    private final ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffectHandler
            chooseDwarfAndAttachAnyNumberOfControlledEquipmentHandler;
    private final EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffectHandler
            benthicAnomalyEffectHandler;
    private final EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffectHandler
            olorinSearingLightEffectHandler;
    private final EachOpponentChoosesCreatureToTapAndGoadEffectHandler fellBeastsShriekEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.RedirectAllDamageToChosenCreatureUntilNextTurnEffectHandler
            redirectCreatureDamageHandler;
    private final org.springframework.beans.factory.ObjectProvider<MultiPermanentChoiceHandlerService>
            multiPermanentChoiceHandlerServiceProvider;
    private final org.springframework.beans.factory.ObjectProvider<ChoiceHandlerService> choiceHandlerServiceProvider;

    /** Declines a champion choice, so the permanent is sacrificed instead of exiling another one (CR 702.72a). */
    public void handleChampionDeclined(GameData gameData, Player player, boolean accepted) {
        PendingInteraction.PermanentChoice permanentChoice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (permanentChoice == null || !player.getId().equals(permanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (accepted || !(permanentChoice.context() instanceof PermanentChoiceContext.ChampionCreature champion)) {
            throw new IllegalStateException("Choose the creature to exile");
        }
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearPermanentChoiceContext();
        battlefieldHandler.handleChampionDeclined(gameData, champion);
    }

    /**
     * Completes {@code permanentId} as the answer to {@code choice} when its completion changes
     * nothing before it sacrifices or destroys the chosen permanent (or the batch of permanents
     * chosen so far, for the "each player chooses ... then destroy" contexts). A creature it would kill that has
     * competing death replacements, one of them keeping the card, is then asked about first
     * (CR 616.1): the completion stops, its controller chooses, and
     * {@link #completeChoiceAgainAfterDeathReplacements} completes the same answer again with that
     * choice. Returns {@code false}, doing nothing, for any other choice.
     */
    private boolean completeAskingDeathReplacementsFirst(GameData gameData,
                                                         PendingInteraction.PermanentChoice choice,
                                                         UUID permanentId) {
        if (!killsBeforeChangingAnything(choice.context())) {
            return false;
        }
        runAskingDeathReplacementsFirst(gameData,
                () -> completeKillingChoice(gameData, permanentId, choice.context()),
                new GameData.PermanentChoiceReplay(choice, permanentId));
        return true;
    }

    /**
     * Runs {@code decline}, the completion of {@code player} declining {@code ability}, a "you may
     * sacrifice" prompt whose decline changes nothing before it sacrifices the picks collected from
     * the players before (Innocent Traveler, Brain Gorgers). A creature it would kill that has
     * competing death replacements is then asked about before it leaves (CR 616.1), and the decline
     * is completed again through {@code ability}'s may handler once its controller has chosen.
     * {@code decline} must close the window (an {@link InputCompletionService} epilogue does) before
     * anything that isn't part of that sacrifice.
     */
    public void declineAskingDeathReplacementsFirst(GameData gameData, Player player, PendingMayAbility ability,
                                                    Runnable decline) {
        runAskingDeathReplacementsFirst(gameData, decline, new GameData.MayDeclineReplay(player, ability));
    }

    /**
     * Runs {@code completion}, the completion of an answered choice that changes nothing before it
     * kills, with the death replacement window open. A creature it would kill that has competing
     * death replacements is then asked about before it leaves (CR 616.1): the completion stops,
     * {@code replay} is stored, and {@link #completeChoiceAgainAfterDeathReplacements} completes the
     * same answer again once every replacement is chosen. The window is closed when this returns.
     */
    public void runAskingDeathReplacementsFirst(GameData gameData, Runnable completion,
                                                GameData.DeathReplacementChoiceReplay replay) {
        boolean rerunCurrentEffect = gameData.rerunCurrentEffectAfterInteraction;
        gameData.deathReplacementsCanRerunEffect = true;
        boolean stopped = false;
        try {
            completion.run();
        } catch (DeathReplacementChoicePending pending) {
            stopped = true;
            // The stack object's parked resolution stays as it is; only this completion is redone.
            gameData.rerunCurrentEffectAfterInteraction = rerunCurrentEffect;
            gameData.pendingDeathReplacementChoiceReplay = replay;
            inputCompletionService.publishStateAfterInput(gameData);
        } finally {
            gameData.deathReplacementsCanRerunEffect = false;
            if (!stopped) {
                gameData.chosenDeathReplacementsForRerun.clear();
            }
        }
    }

    /**
     * Completes the permanent choice, multi-permanent choice, list choice or declined may prompt stopped by
     * {@link #runAskingDeathReplacementsFirst} again with the same answer, once every death
     * replacement it asked about has been chosen; its deaths consume
     * {@link GameData#chosenDeathReplacementsForRerun}.
     */
    public void completeChoiceAgainAfterDeathReplacements(GameData gameData) {
        GameData.DeathReplacementChoiceReplay replay = gameData.pendingDeathReplacementChoiceReplay;
        gameData.pendingDeathReplacementChoiceReplay = null;
        switch (replay) {
            case GameData.PermanentChoiceReplay choiceReplay ->
                    completeAskingDeathReplacementsFirst(gameData, choiceReplay.choice(), choiceReplay.permanentId());
            case GameData.MultiPermanentChoiceReplay multiReplay ->
                    multiPermanentChoiceHandlerServiceProvider.getObject()
                            .completeAskingDeathReplacementsFirst(
                                    gameData, multiReplay.choice(), multiReplay.permanentIds());
            case GameData.MayDeclineReplay declineReplay ->
                    mayEffectHandlerRegistry.getHandler(declineReplay.ability().effects().getFirst())
                            .handle(gameData, declineReplay.player(), false, declineReplay.ability());
            case GameData.ListChoiceReplay listReplay ->
                    choiceHandlerServiceProvider.getObject().completeListChoiceAgainAfterDeathReplacements(
                            gameData, listReplay);
        }
    }

    /**
     * Sacrifices {@code permanentId}, the only candidate {@code playerId} has after answering "yes" to
     * a "you may sacrifice" prompt, through the same completion as choosing it in {@code context}'s
     * permanent choice. A creature with competing death replacements is then asked about before it
     * leaves (CR 616.1), and the completion is redone with that answer, exactly as for a chosen one.
     */
    public void completeLoneCandidateAskingDeathReplacementsFirst(GameData gameData, UUID playerId,
                                                                  UUID permanentId,
                                                                  PermanentChoiceContext context) {
        PendingInteraction.PermanentChoice choice = new PendingInteraction.PermanentChoice(
                playerId, java.util.List.of(permanentId), java.util.List.of(), context, "");
        if (!completeAskingDeathReplacementsFirst(gameData, choice, permanentId)) {
            throw new IllegalStateException("Not a sacrifice or destroy choice: " + context);
        }
    }

    private static boolean killsBeforeChangingAnything(PermanentChoiceContext context) {
        return context instanceof PermanentChoiceContext.SacrificeCreature
                || context instanceof PermanentChoiceContext.DestroyChosenCreature destroy
                        && !destroy.exile() && !destroy.shuffleIntoOwnersLibrary()
                || context instanceof PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToDestroy
                || context instanceof PermanentChoiceContext.EachOpponentChoosesPermanentToDestroy
                || context instanceof PermanentChoiceContext.EachPlayerChoosesCreatureToDestroy
                || context instanceof PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife
                || context instanceof PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness
                || context instanceof PermanentChoiceContext.SacrificeCreatureThenSearchLibrary
                || context instanceof PermanentChoiceContext.TargetPlayerSacrificesCreatureThenDrawsPower
                || context instanceof PermanentChoiceContext.TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeast
                || context instanceof PermanentChoiceContext.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughness
                || context instanceof PermanentChoiceContext.TargetPlayerSacrificesCreatureOrCreatesToken
                || context instanceof PermanentChoiceContext.TargetPlayerSacrificesPermanentThenDealsManaValueDamage
                || context instanceof PermanentChoiceContext.TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicate
                || context instanceof PermanentChoiceContext.SacrificeCreatureCreateTokensIfSubtype
                || context instanceof PermanentChoiceContext.OpponentChoosesPermanentToSacrifice
                || context instanceof PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature
                || context instanceof PermanentChoiceContext.SacrificeOneOfTwo
                || context instanceof PermanentChoiceContext.SacrificeOneOfTwoThenCounterOnOther
                || context instanceof PermanentChoiceContext.SacrificeOneOfTwoThenReturnOtherToHand
                || context instanceof PermanentChoiceContext.SacrificePermanentAndReturnTargetCards
                || context instanceof PermanentChoiceContext.SacrificePermanentAndBoostSelf
                || context instanceof PermanentChoiceContext.SacrificePermanentAndGrantKeywordSelf
                || context instanceof PermanentChoiceContext.SacrificeCreatureCreateTokensEqualToToughness
                || context instanceof PermanentChoiceContext.SacrificeAnotherCreatureDrawAndMayPutPermanent
                || context instanceof PermanentChoiceContext.SacrificeCreatureCreateSizedTokenEqualToPower
                || context instanceof PermanentChoiceContext.SacrificeArtifactForDividedDamage
                || context instanceof PermanentChoiceContext.SacrificeAnotherCreatureDealPowerDamage
                || context instanceof PermanentChoiceContext.SacrificeAnotherCreatureGainLifeAndDraw
                || context instanceof PermanentChoiceContext.ExploitSacrifice
                || context instanceof PermanentChoiceContext.SacrificePermanentThen
                || context instanceof PermanentChoiceContext.SacrificeOtherCreatureThenRevealUntilLowerManaValue
                || context instanceof PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealMatchingCreature
                || context instanceof PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureType
                || context instanceof PermanentChoiceContext.SacrificeCreatureThenMassDamageEqualToPower
                || context instanceof PermanentChoiceContext.MaySacrificeForCounterOnSource
                || context instanceof PermanentChoiceContext.GargantuanGorillaSacrificeForest
                || context instanceof PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndCounter
                || context instanceof PermanentChoiceContext.AnyOpponentSacrificeNontokenCreatureForTapAndSeekLand
                || context instanceof PermanentChoiceContext.AnyOpponentSacrificeCreatureForTransform
                || context instanceof PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw
                || context instanceof PermanentChoiceContext.AnyPlayerMaySacrificeLandPutSourceOnTop
                || context instanceof PermanentChoiceContext.AnyPlayerMaySacrificeCreatureToCounterSpell
                || context instanceof PermanentChoiceContext.ForcedCostOrElse forcedCost
                        && forcedCost.effect().forcedCost() instanceof SacrificePermanentCost
                || context instanceof PermanentChoiceContext.TormentSacrifice;
    }

    private void completeKillingChoice(GameData gameData, UUID permanentId, PermanentChoiceContext context) {
        switch (context) {
            case PermanentChoiceContext.SacrificeCreature sacrificeCreature ->
                    battlefieldHandler.handleSacrificeCreature(gameData, permanentId, sacrificeCreature);
            case PermanentChoiceContext.DestroyChosenCreature destroyChosenCreature ->
                    battlefieldHandler.handleDestroyChosenCreature(gameData, permanentId, destroyChosenCreature);
            case PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToDestroy destroy ->
                    battlefieldHandler.handleEachOpponentChoosesGreatestPowerCreatureToDestroy(
                            gameData, permanentId, destroy);
            case PermanentChoiceContext.EachOpponentChoosesPermanentToDestroy destroy ->
                    battlefieldHandler.handleEachOpponentChoosesPermanentToDestroy(gameData, permanentId, destroy);
            case PermanentChoiceContext.EachPlayerChoosesCreatureToDestroy destroy ->
                    battlefieldHandler.handleEachPlayerChoosesCreatureToDestroy(gameData, permanentId, destroy);
            case PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife sacrificeOpp ->
                    battlefieldHandler.handleSacrificeCreatureOpponentsLoseLife(gameData, permanentId, sacrificeOpp);
            case PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness sacrificeGainLife ->
                    battlefieldHandler.handleSacrificeCreatureControllerGainsLifeEqualToToughness(
                            gameData, permanentId, sacrificeGainLife);
            case PermanentChoiceContext.SacrificeCreatureThenSearchLibrary sacrificeSearch ->
                    battlefieldHandler.handleSacrificeCreatureThenSearchLibrary(gameData, permanentId, sacrificeSearch);
            case PermanentChoiceContext.TargetPlayerSacrificesCreatureThenDrawsPower targetPlayerSacrifice ->
                    battlefieldHandler.handleTargetPlayerSacrificesCreatureThenDrawsPower(
                            gameData, permanentId, targetPlayerSacrifice);
            case PermanentChoiceContext.TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeast targetPlayerSacrifice ->
                    battlefieldHandler.handleTargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeast(
                            gameData, permanentId, targetPlayerSacrifice);
            case PermanentChoiceContext.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughness targetPlayerSacrifice ->
                    battlefieldHandler.handleTargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughness(
                            gameData, permanentId, targetPlayerSacrifice);
            case PermanentChoiceContext.TargetPlayerSacrificesCreatureOrCreatesToken targetPlayerSacrificeOrToken ->
                    battlefieldHandler.handleTargetPlayerSacrificesCreatureOrCreatesToken(
                            gameData, permanentId, targetPlayerSacrificeOrToken);
            case PermanentChoiceContext.TargetPlayerSacrificesPermanentThenDealsManaValueDamage targetPlayerSacrifice ->
                    battlefieldHandler.handleTargetPlayerSacrificesPermanentThenDealsManaValueDamage(
                            gameData, permanentId, targetPlayerSacrifice);
            case PermanentChoiceContext.TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicate targetPlayerSacrifice ->
                    battlefieldHandler.handleTargetPlayerSacrificesNontokenCreatureThenConjuresDuplicate(
                            gameData, permanentId, targetPlayerSacrifice);
            case PermanentChoiceContext.SacrificeCreatureCreateTokensIfSubtype sacrificeTokens ->
                    battlefieldHandler.handleSacrificeCreatureCreateTokensIfSubtype(gameData, permanentId, sacrificeTokens);
            case PermanentChoiceContext.OpponentChoosesPermanentToSacrifice sacrificeChoice ->
                    battlefieldHandler.handleOpponentChoosesPermanentToSacrifice(gameData, permanentId, sacrificeChoice);
            case PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature loseLifeAndSacrifice ->
                    battlefieldHandler.handleEachTargetPlayerLosesLifeAndSacrificesCreature(
                            gameData, permanentId, loseLifeAndSacrifice);
            case PermanentChoiceContext.SacrificeOneOfTwo sacrificeOneOfTwo ->
                    battlefieldHandler.handleSacrificeOneOfTwo(gameData, permanentId, sacrificeOneOfTwo);
            case PermanentChoiceContext.SacrificeOneOfTwoThenCounterOnOther sacrificeOneOfTwo ->
                    battlefieldHandler.handleSacrificeOneOfTwoThenCounterOnOther(gameData, permanentId, sacrificeOneOfTwo);
            case PermanentChoiceContext.SacrificeOneOfTwoThenReturnOtherToHand sacrificeOneOfTwo ->
                    battlefieldHandler.handleSacrificeOneOfTwoThenReturnOtherToHand(gameData, permanentId, sacrificeOneOfTwo);
            case PermanentChoiceContext.SacrificePermanentAndReturnTargetCards spar ->
                    battlefieldHandler.handleSacrificePermanentAndReturnTargetCards(gameData, permanentId, spar);
            case PermanentChoiceContext.SacrificePermanentAndBoostSelf spabs ->
                    battlefieldHandler.handleSacrificePermanentAndBoostSelf(gameData, permanentId, spabs);
            case PermanentChoiceContext.SacrificePermanentAndGrantKeywordSelf spagks ->
                    battlefieldHandler.handleSacrificePermanentAndGrantKeywordSelf(gameData, permanentId, spagks);
            case PermanentChoiceContext.SacrificeCreatureCreateTokensEqualToToughness scct ->
                    battlefieldHandler.handleSacrificeCreatureCreateTokensEqualToToughness(gameData, permanentId, scct);
            case PermanentChoiceContext.SacrificeAnotherCreatureDrawAndMayPutPermanent venom ->
                    battlefieldHandler.handleSacrificeAnotherCreatureDrawAndMayPutPermanent(gameData, permanentId, venom);
            case PermanentChoiceContext.SacrificeCreatureCreateSizedTokenEqualToPower scsp ->
                    battlefieldHandler.handleSacrificeCreatureCreateSizedTokenEqualToPower(gameData, permanentId, scsp);
            case PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd ->
                    battlefieldHandler.handleSacrificeArtifactForDividedDamage(gameData, permanentId, sadd);
            case PermanentChoiceContext.SacrificeAnotherCreatureDealPowerDamage sacpd ->
                    battlefieldHandler.handleSacrificeAnotherCreatureDealPowerDamage(gameData, permanentId, sacpd);
            case PermanentChoiceContext.SacrificeAnotherCreatureGainLifeAndDraw sacgld ->
                    battlefieldHandler.handleSacrificeAnotherCreatureGainLifeAndDraw(gameData, permanentId, sacgld);
            case PermanentChoiceContext.ExploitSacrifice exploitSac ->
                    battlefieldHandler.handleExploitSacrifice(gameData, permanentId, exploitSac);
            case PermanentChoiceContext.SacrificePermanentThen spt ->
                    battlefieldHandler.handleSacrificePermanentThen(gameData, permanentId, spt);
            case PermanentChoiceContext.SacrificeOtherCreatureThenRevealUntilLowerManaValue kethek ->
                    battlefieldHandler.handleSacrificeOtherCreatureThenRevealUntilLowerManaValue(
                            gameData, permanentId, kethek);
            case PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealMatchingCreature descendantsFury ->
                    battlefieldHandler.handleSacrificeOneOfCombatDamageDealersThenRevealMatchingCreature(
                            gameData, permanentId, descendantsFury);
            case PermanentChoiceContext.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureType descendantsFury -> {
                gameData.playerBattlefields.getOrDefault(descendantsFury.controllerId(), java.util.List.of()).stream()
                        .filter(permanent -> permanent.getId().equals(permanentId))
                        .findFirst()
                        .ifPresent(permanent -> descendantsFuryEffectHandler.resolveAfterChoice(
                                gameData, descendantsFury.sourceCard(), descendantsFury.controllerId(), permanent));
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            case PermanentChoiceContext.SacrificeCreatureThenMassDamageEqualToPower rupture ->
                    battlefieldHandler.handleSacrificeCreatureThenMassDamageEqualToPower(gameData, permanentId, rupture);
            case PermanentChoiceContext.MaySacrificeForCounterOnSource msfc ->
                    battlefieldHandler.handleMaySacrificeForCounterOnSource(gameData, permanentId, msfc);
            case PermanentChoiceContext.GargantuanGorillaSacrificeForest ggsf ->
                    battlefieldHandler.handleGargantuanGorillaSacrificeForest(gameData, permanentId, ggsf);
            case PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndCounter aosc ->
                    battlefieldHandler.handleAnyOpponentSacrificeCreatureForTapAndCounter(gameData, permanentId, aosc);
            case PermanentChoiceContext.AnyOpponentSacrificeNontokenCreatureForTapAndSeekLand ansc ->
                    battlefieldHandler.handleAnyOpponentSacrificeNontokenCreatureForTapAndSeekLand(
                            gameData, permanentId, ansc);
            case PermanentChoiceContext.AnyOpponentSacrificeCreatureForTransform aosc ->
                    battlefieldHandler.handleAnyOpponentSacrificeCreatureForTransform(gameData, permanentId, aosc);
            case PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw aosc ->
                    battlefieldHandler.handleAnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw(
                            gameData, permanentId, aosc);
            case PermanentChoiceContext.AnyPlayerMaySacrificeLandPutSourceOnTop ams ->
                    battlefieldHandler.handleAnyPlayerMaySacrificeLandPutSourceOnTop(gameData, permanentId, ams);
            case PermanentChoiceContext.AnyPlayerMaySacrificeCreatureToCounterSpell ams ->
                    battlefieldHandler.handleAnyPlayerMaySacrificeCreatureToCounterSpell(gameData, permanentId, ams);
            case PermanentChoiceContext.ForcedCostOrElse forcedCostOrElse ->
                    battlefieldHandler.handleForcedCostOrElse(gameData, permanentId, forcedCostOrElse);
            case PermanentChoiceContext.TormentSacrifice tormentSacrifice ->
                    battlefieldHandler.handleTormentSacrifice(gameData, permanentId, tormentSacrifice);
            default -> throw new IllegalStateException("Not a sacrifice or destroy choice: " + context);
        }
    }

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        PendingInteraction.PermanentChoice permanentChoice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (permanentChoice == null || !player.getId().equals(permanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = permanentChoice.validIds();

        // Validate before touching interaction state: a rejected answer must leave the prompt
        // standing so the player can answer again. Clearing first and then throwing destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry, wedging
        // the game (and with it deferPlayerLossCheck) on nothing worse than a stale client answer.
        if (!validIds.contains(permanentId)) {
            throw new IllegalStateException(
                    triggerHandler.rejectedChoiceMessage(gameData, permanentChoice.context(), permanentId));
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearPermanentChoiceContext();

        if (completeAskingDeathReplacementsFirst(gameData, permanentChoice, permanentId)) {
            return;
        }

        PermanentChoiceContext context = permanentChoice.context();

        if (context instanceof PermanentChoiceContext.RedirectCreatureDamage redirect) {
            redirectCreatureDamageHandler.completeChoice(gameData, redirect.controllerId(), permanentId);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.CelestialJudgmentChoice celestialJudgmentChoice) {
            celestialJudgmentEffectHandler.completeChoice(gameData, permanentId, celestialJudgmentChoice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.LibraryNinjutsu libraryNinjutsu) {
            libraryChoiceHandlerService.completeLibraryNinjutsuChoice(gameData, player, permanentId, libraryNinjutsu);
        } else if (context instanceof PermanentChoiceContext.GutmornDiscardedCardPlayerChoice gutmornChoice) {
            gutmornEffectHandler.completeChoice(gameData, permanentId, gutmornChoice);
        } else if (context instanceof PermanentChoiceContext.AuraEntryBatchChoice auraEntry) {
            battlefieldHandler.handleAuraEntryBatchChoice(gameData, permanentId, auraEntry);
        } else if (context instanceof PermanentChoiceContext.CloneCopy) {
            battlefieldHandler.handleCloneCopy(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.TurnFaceUpCopy turnFaceUpCopy) {
            battlefieldHandler.handleTurnFaceUpCopy(gameData, permanentId, turnFaceUpCopy);
        } else if (context instanceof PermanentChoiceContext.CipherEncode) {
            battlefieldHandler.handleCipherEncode(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.Teach) {
            battlefieldHandler.handleTeach(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.CreateTokensAndAttachEquipment attachEquipment) {
            battlefieldHandler.handleCreateTokensAndAttachEquipment(gameData, permanentId, attachEquipment);
        } else if (context instanceof PermanentChoiceContext.AttachEquipmentToCreature attachEquip) {
            battlefieldHandler.handleAttachEquipmentToCreature(gameData, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AttachSacrificedEquipmentToTarget attachEquip) {
            battlefieldHandler.handleAttachSacrificedEquipmentToTarget(gameData, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AttachControlledEquipmentToTargetCreature attachEquip) {
            battlefieldHandler.handleAttachControlledEquipmentToTargetCreature(gameData, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.ChooseDwarfAndAttachAnyNumberOfControlledEquipment chooseDwarf) {
            chooseDwarfAndAttachAnyNumberOfControlledEquipmentHandler.completeChoice(
                    gameData, playerId, permanentId, chooseDwarf);
        } else if (context instanceof PermanentChoiceContext.AttachEquipmentToSamurai attachEquip) {
            battlefieldHandler.handleAttachEquipmentToSamurai(gameData, playerId, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AttachMatchingEquipmentToCreatedPermanent attachEquip) {
            battlefieldHandler.handleAttachMatchingEquipmentToCreatedPermanent(gameData, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AttachEquipmentToSamuraiTarget attachEquip) {
            battlefieldHandler.handleAttachEquipmentToSamuraiTarget(gameData, playerId, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AttachOneOfEquipmentToCreature attachEquip) {
            battlefieldHandler.handleAttachOneOfEquipmentToCreature(
                    gameData, playerId, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
            battlefieldHandler.handleAuraGraft(gameData, permanentId, auraGraft);
        } else if (context instanceof PermanentChoiceContext.AttachAllAurasToAnotherPermanent attachAll) {
            battlefieldHandler.handleAttachAllAurasToAnotherPermanent(gameData, permanentId, attachAll);
        } else if (context instanceof PermanentChoiceContext.ReattachSourceAuraAfterSacrifice reattach) {
            battlefieldHandler.handleReattachSourceAuraAfterSacrifice(gameData, permanentId, reattach);
        } else if (context instanceof PermanentChoiceContext.AttachSourceAuraToChosenPermanent attachAura) {
            battlefieldHandler.handleAttachSourceAuraToChosenPermanent(gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.AttachSelectedAuraToLegalTarget attachAura) {
            attachSelectedAurasHandler.completeChoice(gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.LyndeCurseChoice lyndeCurseChoice) {
            attachCurseToOpponentAndDrawEffectHandler.completeCurseChoice(
                    gameData, permanentId, lyndeCurseChoice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.InventoryManagementAttachment attachmentChoice) {
            inventoryManagementEffectHandler.completeCreatureChoice(gameData, permanentId, attachmentChoice);
        } else if (context instanceof PermanentChoiceContext.PermanentAuctionAuraHost auctionHost) {
            permanentAuctionService.completeAuraHostChoice(gameData, permanentId, auctionHost);
        } else if (context instanceof PermanentChoiceContext.AttachReturnedAuraToCreature attachAura) {
            returnAurasFromGraveyardAttachedToCreaturesEffectHandler.completeCreatureChoice(
                    gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.TransformChosenPermanent) {
            battlefieldHandler.handleTransformChosenPermanent(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.AttachTargetAuraToAnotherPermanentOfSameType attachAura) {
            battlefieldHandler.handleAttachTargetAuraToAnotherPermanentOfSameType(gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.AttachTargetAuraToAnotherPermanentWithSameController attachAura) {
            battlefieldHandler.handleAttachTargetAuraToAnotherPermanentWithSameController(gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
            battlefieldHandler.handleLegendRule(gameData, playerId, permanentId, legendRule);
        } else if (context instanceof PermanentChoiceContext.ForcedCostOrElse forcedCostOrElse) {
            battlefieldHandler.handleForcedCostOrElse(gameData, permanentId, forcedCostOrElse);
        } else if (context instanceof PermanentChoiceContext.DealDamageToOneOfTwoThenOtherCantBlock dealDamage) {
            battlefieldHandler.handleDealDamageToOneOfTwoThenOtherCantBlock(gameData, permanentId, dealDamage);
        } else if (context instanceof PermanentChoiceContext.PackHuntDamageAssignment packHunt) {
            battlefieldHandler.handlePackHuntDamageAssignment(gameData, permanentId, packHunt);
        } else if (context instanceof PermanentChoiceContext.CannibalizeChoice cannibalize) {
            battlefieldHandler.handleCannibalizeChoice(gameData, permanentId, cannibalize);
        } else if (context instanceof PermanentChoiceContext.PutCounterOnEitherTarget counterChoice) {
            battlefieldHandler.handlePutCounterOnEitherTarget(gameData, permanentId, counterChoice);
        } else if (context instanceof PermanentChoiceContext.LandEquilibriumSacrifice landEquilibrium) {
            battlefieldHandler.handleLandEquilibriumSacrifice(gameData, permanentId, landEquilibrium);
        } else if (context instanceof PermanentChoiceContext.TormentSacrifice tormentSacrifice) {
            battlefieldHandler.handleTormentSacrifice(gameData, permanentId, tormentSacrifice);
        } else if (context instanceof PermanentChoiceContext.ExquisiteArchangelLossReplacement lossReplacement) {
            battlefieldHandler.handleExquisiteArchangelLossReplacement(gameData, permanentId, lossReplacement);
        } else if (context instanceof PermanentChoiceContext.DestroyChosenCreature destroyChosenCreature) {
            battlefieldHandler.handleDestroyChosenCreature(gameData, permanentId, destroyChosenCreature);
        } else if (context instanceof PermanentChoiceContext.ExileChosenPermanent exileChosenPermanent) {
            battlefieldHandler.handleExileChosenPermanent(gameData, permanentId, exileChosenPermanent);
        } else if (context instanceof PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents exileMatching) {
            battlefieldHandler.handleExilePermanentThenExileMatchingPermanents(gameData, permanentId, exileMatching);
        } else if (context instanceof PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary exileTwo) {
            battlefieldHandler.handleExileTwoPermanentsThenSearchLibrary(gameData, permanentId, exileTwo);
        } else if (context instanceof PermanentChoiceContext.GildedAmbusherChoice gildedAmbusherChoice) {
            gildedAmbusherEffectHandler.completeChoice(gameData, permanentId, gildedAmbusherChoice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.ExileCombatOpponent exileCombatOpponent) {
            battlefieldHandler.handleExileCombatOpponent(gameData, permanentId, exileCombatOpponent);
        } else if (context instanceof PermanentChoiceContext.DefendingPlayerChoosesCreatureToBlock chooseBlocker) {
            battlefieldHandler.handleDefendingPlayerChoosesCreatureToBlock(gameData, permanentId, chooseBlocker);
        } else if (context instanceof PermanentChoiceContext.AttackingPlayerChoosesCreatureToBoost chooseBoost) {
            battlefieldHandler.handleAttackingPlayerChoosesCreatureToBoost(gameData, permanentId, chooseBoost);
        } else if (context instanceof PermanentChoiceContext.MeldPartnerChoice meldChoice) {
            battlefieldHandler.handleMeldPartnerChoice(gameData, permanentId, meldChoice);
        } else if (context instanceof PermanentChoiceContext.BalduvianWarlordChoosesAttacker chooseAttacker) {
            battlefieldHandler.handleBalduvianWarlordChoosesAttacker(gameData, permanentId, chooseAttacker);
        } else if (context instanceof PermanentChoiceContext.OpponentChoosesCreatureYouGainControl richesChoice) {
            battlefieldHandler.handleOpponentChoosesCreatureYouGainControl(gameData, permanentId, richesChoice);
        } else if (context instanceof PermanentChoiceContext.SeizeTheSpotlightCreatureChoice seizeChoice) {
            battlefieldHandler.handleSeizeTheSpotlightCreatureChoice(gameData, permanentId, seizeChoice);
        } else if (context instanceof PermanentChoiceContext.OrderOfSuccession orderOfSuccession) {
            battlefieldHandler.handleOrderOfSuccessionChoice(gameData, permanentId, orderOfSuccession);
        } else if (context instanceof PermanentChoiceContext.CaughtInAParallelUniverseCreatureChoice parallelChoice) {
            battlefieldHandler.handleCaughtInAParallelUniverseCreatureChoice(gameData, permanentId, parallelChoice);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesCreatureToExileWithSource exileChoice) {
            battlefieldHandler.handleEachOpponentChoosesCreatureToExileWithSource(gameData, permanentId, exileChoice);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamage searingLightChoice) {
            battlefieldHandler.handleEachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamage(
                    gameData, permanentId, searingLightChoice);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesCreatureToTapAndGoad fellBeastsShriekChoice) {
            battlefieldHandler.handleEachOpponentChoosesCreatureToTapAndGoad(gameData, permanentId,
                    fellBeastsShriekChoice);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesCreatureForTokenCopy benthicChoice) {
            battlefieldHandler.handleEachOpponentChoosesCreatureForTokenCopy(gameData, permanentId, benthicChoice);
        } else if (context instanceof PermanentChoiceContext.ChooseBenthicAnomalyCopy benthicCopyChoice) {
            benthicAnomalyEffectHandler.completeCopyChoice(gameData, permanentId, benthicCopyChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentGainsControlOfSource chooseOpponent) {
            battlefieldHandler.handleChooseOpponentGainsControlOfSource(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentDrawAndUntap chooseOpponent) {
            battlefieldHandler.handleChooseOpponentDrawAndUntap(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ToymakersTrapOpponentChoice chooseOpponent) {
            toymakersTrapEffectHandler.completeOpponentChoice(gameData, permanentId, chooseOpponent);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentForTargetingRelay chooseOpponent) {
            battlefieldHandler.handleChooseOpponentForTargetingRelay(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentEachCreatesTokens chooseOpponent) {
            battlefieldHandler.handleChooseOpponentEachCreatesTokens(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentReturnsCardFromGraveyard chooseOpponent) {
            battlefieldHandler.handleChooseOpponentReturnsCardFromGraveyard(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentThenSearchLandDifference chooseOpponent) {
            battlefieldHandler.handleChooseOpponentThenSearchLandDifference(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.OrzhovAdvokistCreatureChoice advokistChoice) {
            battlefieldHandler.handleOrzhovAdvokistCreatureChoice(gameData, permanentId, advokistChoice);
        } else if (context instanceof PermanentChoiceContext.InfernalOfferingOpponentChoice chooseOpponent) {
            infernalOfferingEffectHandler.completeOpponentChoice(gameData, permanentId, chooseOpponent);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.InfernalOfferingCreatureChoice creatureChoice) {
            infernalOfferingEffectHandler.completeCreatureChoice(gameData, permanentId, creatureChoice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.RiskyMoveCreatureChoice riskyMove) {
            riskyMoveEffectHandler.completeCreatureChoice(gameData, permanentId, riskyMove);
        } else if (context instanceof PermanentChoiceContext.RiskyMoveOpponentChoice riskyMove) {
            riskyMoveEffectHandler.completeOpponentChoice(gameData, permanentId, riskyMove);
        } else if (context instanceof PermanentChoiceContext.ChooseAnotherPlayerGainsControlOfTargetPermanent chooseAnotherPlayer) {
            battlefieldHandler.handleChooseAnotherPlayerGainsControlOfTargetPermanent(
                    gameData, permanentId, chooseAnotherPlayer);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentGainsControlOfSourceAndMatchingPermanents chooseOpponent) {
            battlefieldHandler.handleTapAndChooseOpponentGainsControl(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentForSelfFlicker chooseOpponent) {
            battlefieldHandler.handleChooseOpponentForSelfFlicker(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentForPermanentSacrifice chooseOpponent) {
            battlefieldHandler.handleChooseOpponentForPermanentSacrifice(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentForPermanentExile chooseOpponent) {
            battlefieldHandler.handleChooseOpponentForPermanentExile(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentForSubtype chooseOpponent) {
            battlefieldHandler.handleChooseOpponentForSubtype(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.OpponentChoosesPermanentToExile exileChoice) {
            battlefieldHandler.handleOpponentChoosesPermanentToExile(gameData, permanentId, exileChoice);
        } else if (context instanceof PermanentChoiceContext.PermanentYouControlToExile exileChoice) {
            battlefieldHandler.handlePermanentYouControlToExile(gameData, permanentId, exileChoice);
        } else if (context instanceof PermanentChoiceContext.ExilePermanentYouControlThenCreateToken exileChoice) {
            battlefieldHandler.handleExilePermanentYouControlThenCreateToken(gameData, permanentId, exileChoice);
        } else if (context instanceof PermanentChoiceContext.TakeoverCreatureToExile takeoverChoice) {
            battlefieldHandler.handleTakeoverCreatureToExile(gameData, permanentId, takeoverChoice);
        } else if (context instanceof PermanentChoiceContext.ExileAnotherCreatureAndConjureRandomCreature exileChoice) {
            battlefieldHandler.handleExileAnotherCreatureAndConjureRandomCreature(
                    gameData, permanentId, exileChoice);
        } else if (context instanceof PermanentChoiceContext.MurmursFromBeyondOpponentChoice murmursChoice) {
            murmursFromBeyondEffectHandler.completeOpponentChoice(gameData, permanentId, murmursChoice);
        } else if (context instanceof PermanentChoiceContext.AnimalMagnetismOpponentChoice animalMagnetismChoice) {
            animalMagnetismEffectHandler.completeOpponentChoice(gameData, permanentId, animalMagnetismChoice);
        } else if (context instanceof PermanentChoiceContext.AllureOfTheUnknownOpponentChoice allureChoice) {
            allureOfTheUnknownEffectHandler.completeOpponentChoice(gameData, permanentId, allureChoice);
        } else if (context instanceof PermanentChoiceContext.MemoriesReturningOpponentChoice memoriesChoice) {
            memoriesReturningEffectHandler.completeOpponentChoice(gameData, permanentId, memoriesChoice);
        } else if (context instanceof PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy echoChamberChoice) {
            battlefieldHandler.handleOpponentChoosesCreatureTheyControlToCopy(gameData, permanentId, echoChamberChoice);
        } else if (context instanceof PermanentChoiceContext.ChooseControlledArtifactOrCreatureToCopy chooseCopy) {
            battlefieldHandler.handleChooseControlledArtifactOrCreatureToCopy(gameData, permanentId, chooseCopy);
        } else if (context instanceof PermanentChoiceContext.AnimateChosenOwnPermanent animateChoice) {
            battlefieldHandler.handleAnimateChosenOwnPermanent(gameData, permanentId, animateChoice);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentCreatureThenBoostOthers zenosChoice) {
            battlefieldHandler.handleChooseOpponentCreatureThenBoostOthers(gameData, permanentId, zenosChoice);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentCreatureAndPerpetuallyBoost aloraChoice) {
            battlefieldHandler.handleChooseOpponentCreatureAndPerpetuallyBoost(gameData, permanentId, aloraChoice);
        } else if (context instanceof PermanentChoiceContext.GuidedPassageOpponentChoice guidedPassageChoice) {
            guidedPassageEffectHandler.completeOpponentChoice(gameData, permanentId, guidedPassageChoice);
        } else if (context instanceof PermanentChoiceContext.MausoleumTurnkeyOpponentChoice mausoleumTurnkeyChoice) {
            returnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler.completeOpponentChoice(
                    gameData, permanentId, mausoleumTurnkeyChoice);
        } else if (context instanceof PermanentChoiceContext.AwakenTheMaelstromPermanentCopyChoice copyChoice) {
            battlefieldHandler.handleAwakenTheMaelstromPermanentCopyChoice(gameData, permanentId, copyChoice);
        } else if (context instanceof PermanentChoiceContext.ChosenPermanentCopyChoice copyChoice) {
            battlefieldHandler.handleChosenPermanentCopyChoice(gameData, permanentId, copyChoice);
        } else if (context instanceof PermanentChoiceContext.ExploreOrderChoice exploreOrder) {
            exploreEachControlledCreatureEffectHandler.completeChoice(gameData, permanentId, exploreOrder);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.ChangeSpellTargetToSource changeTarget) {
            changeTargetOfTargetSpellToSourceEffectHandler.completeChoice(gameData, permanentId, changeTarget);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.RemoveCounterFromControlledCreatureThenDraw removeCounter) {
            removeCounterFromControlledCreatureThenDrawEffectHandler.completeChoice(
                    gameData, permanentId, removeCounter);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.DealDamageToChosenCreature damageChoice) {
            dealDamageToChosenCreatureEffectHandler.completeChoice(gameData, permanentId, damageChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.BoostEquippedOrMatchingCreatureChoice boostChoice) {
            boostEquippedCreatureUntilEndOfTurnEffectHandler.completeChoice(gameData, permanentId, boostChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.AwakenTheMaelstromCounterCreatureChoice) {
            battlefieldHandler.handleAwakenTheMaelstromCounterCreatureChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.OpponentMayGainControlOfCreatureYouControl opponentSteal) {
            battlefieldHandler.handleOpponentMayGainControlOfCreatureYouControl(gameData, permanentId, opponentSteal);
        } else if (context instanceof PermanentChoiceContext.GainControlOfDefendingPlayerCreatureAndAttack gainControl) {
            battlefieldHandler.handleGainControlOfDefendingPlayerCreatureAndAttack(gameData, permanentId, gainControl);
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
            battlefieldHandler.handleActivatedAbilityCostChoice(gameData, player, permanentId, costChoice);
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilityOpponentTarget opponentTarget) {
            abilityActivationService.handleOpponentChosenTarget(gameData, player, permanentId, opponentTarget);
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilitySoleOpponentTarget opponentTarget) {
            abilityActivationService.handleSoleOpponentChosenTarget(gameData, player, permanentId, opponentTarget);
        } else if (context instanceof PermanentChoiceContext.GraveyardAbilityCostChoice graveyardCostChoice) {
            battlefieldHandler.handleGraveyardAbilityCostChoice(gameData, player, permanentId, graveyardCostChoice);
        } else if (context instanceof PermanentChoiceContext.HandAbilityCostChoice handCostChoice) {
            battlefieldHandler.handleHandAbilityCostChoice(gameData, player, permanentId, handCostChoice);
        } else if (context instanceof PermanentChoiceContext.MayAbilityTapCostChoice mayTapCostChoice) {
            battlefieldHandler.handleMayAbilityTapCostChoice(gameData, player, permanentId, mayTapCostChoice);
        } else if (context instanceof PermanentChoiceContext.ConniveNextCreatureChoice
                || context instanceof PermanentChoiceContext.ChosenPermanentReference) {
            gameData.pendingEffectResolutionEntry.setChosenPermanentId(permanentId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.BackdraftPlayerChoice) {
            gameData.pendingEffectResolutionEntry.setTargetId(permanentId);
            var sorceries = gameData.getSpellsCastThisTurn(permanentId).stream()
                    .filter(card -> card.hasType(CardType.SORCERY))
                    .map(card -> card.getId()).distinct().toList();
            if (sorceries.size() > 1) {
                playerInputService.beginPermanentChoice(gameData, player.getId(), sorceries,
                        new PermanentChoiceContext.BackdraftSorceryChoice(),
                        "Backdraft — Choose the sorcery whose damage to use.");
            } else {
                gameData.pendingEffectResolutionEntry.setChosenPermanentId(sorceries.getFirst());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.BackdraftSorceryChoice) {
            gameData.pendingEffectResolutionEntry.setChosenPermanentId(permanentId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.LyndeOpponentChoice lyndeOpponentChoice) {
            attachCurseToOpponentAndDrawEffectHandler.completeOpponentChoice(
                    gameData, permanentId, lyndeOpponentChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.ChoosePlayerThenReturnCreatureToHand choosePlayer) {
            battlefieldHandler.handleChoosePlayerThenReturnCreatureToHand(gameData, permanentId, choosePlayer);
        } else if (context instanceof PermanentChoiceContext.ZndrsplatsJudgmentCreatureChoice judgmentChoice) {
            zndrsplatsJudgmentEffectHandler.completeCreatureChoice(gameData, permanentId, judgmentChoice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.TargetOpponentChoosesPlayerForRestriction restriction) {
            targetOpponentChoosesPlayerForRestrictionEffectHandler.completeChoice(gameData, permanentId, restriction);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.BounceBatch bounceBatch) {
            returnPermanentsToHandSimultaneouslyEffectHandler.completeChoice(gameData, permanentId, bounceBatch);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.BounceCreature bounceCreature) {
            battlefieldHandler.handleBounceCreature(gameData, permanentId, bounceCreature);
        } else if (context instanceof PermanentChoiceContext.BouncePermanentThen bounceThen) {
            battlefieldHandler.handleBouncePermanentThen(gameData, permanentId, bounceThen);
        } else if (context instanceof PermanentChoiceContext.BouncePermanentAndPerpetuallyBecomeAngel becomeAngel) {
            battlefieldHandler.handleBouncePermanentAndPerpetuallyBecomeAngel(gameData, permanentId, becomeAngel);
        } else if (context instanceof PermanentChoiceContext.ConjureDuplicateOfCombatDamageDealerChoice conjureChoice) {
            conjureDuplicateOfChosenCombatDamageDealerIntoHandEffectHandler.completeChoice(
                    gameData, permanentId, conjureChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.ReturnPermanentAndPutCounterOnSource returnAndCounter) {
            battlefieldHandler.handleReturnPermanentAndPutCounterOnSource(gameData, permanentId, returnAndCounter);
        } else if (context instanceof PermanentChoiceContext.ReturnCreatureToHandAndPutCountersOnSourceEqualToPower returnAndCounter) {
            battlefieldHandler.handleReturnCreatureToHandAndPutCountersOnSourceEqualToPower(
                    gameData, permanentId, returnAndCounter);
        } else if (context instanceof PermanentChoiceContext.MayReturnPermanentToHandAndEnterWithCounters returnChoice) {
            battlefieldHandler.handleMayReturnPermanentToHandAndEnterWithCounters(gameData, permanentId, returnChoice);
        } else if (context instanceof PermanentChoiceContext.BounceOwnPermanentOrSacrificeSelf) {
            battlefieldHandler.handleBounceOwnPermanentOrSacrificeSelf(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.BouncePermanentOrSacrificeSelf) {
            battlefieldHandler.handleBouncePermanentOrSacrificeSelf(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.SacrificeOwnPermanentOrSacrificeSelf) {
            battlefieldHandler.handleSacrificeOwnPermanentOrSacrificeSelf(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.SacrificePermanentToEnter sacToEnter) {
            battlefieldHandler.handleSacrificePermanentToEnter(gameData, permanentId, sacToEnter);
        } else if (context instanceof PermanentChoiceContext.LandCasualty landCasualty) {
            battlefieldHandler.handleLandCasualty(gameData, permanentId, landCasualty);
        } else if (context instanceof PermanentChoiceContext.ChampionCreature championCreature) {
            battlefieldHandler.handleChampionCreature(gameData, permanentId, championCreature);
        } else if (context instanceof PermanentChoiceContext.Populate populate) {
            battlefieldHandler.handlePopulate(gameData, permanentId, populate);
        } else if (context instanceof PermanentChoiceContext.EsixCreatureChoice esixChoice) {
            esixCreatureCopyHandler.completeChoice(gameData, permanentId, esixChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.PutControlledCreatureOnTopOfLibrary putOnTop) {
            battlefieldHandler.handlePutControlledCreatureOnTopOfLibrary(gameData, permanentId, putOnTop);
        } else if (context instanceof PermanentChoiceContext.SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLand chosenLand) {
            battlefieldHandler.handleSearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLand(
                    gameData, permanentId, chosenLand);
        } else if (context instanceof PermanentChoiceContext.DeepfathomEchoCreatureChoice deepfathomEcho) {
            battlefieldHandler.handleDeepfathomEchoCreatureChoice(gameData, permanentId, deepfathomEcho);
        } else if (context instanceof PermanentChoiceContext.BrudicladTokenChoice brudicladToken) {
            battlefieldHandler.handleBrudicladTokenChoice(gameData, permanentId, brudicladToken);
        } else if (context instanceof PermanentChoiceContext.PolymorphousRushCreatureChoice polymorphousRush) {
            battlefieldHandler.handlePolymorphousRushCreatureChoice(gameData, permanentId, polymorphousRush);
        } else if (context instanceof PermanentChoiceContext.CopySpellForOtherControlledCreatureChoice copyChoice) {
            triggerHandler.handleCopySpellForOtherControlledCreature(gameData, permanentId, copyChoice);
        } else if (context instanceof PermanentChoiceContext.CopySpellForAnotherOpponentPermanentChoice copyChoice) {
            triggerHandler.handleCopySpellForAnotherOpponentPermanent(gameData, permanentId, copyChoice);
        } else if (context instanceof PermanentChoiceContext.DemonstrateOpponentChoice demonstrate) {
            triggerHandler.handleDemonstrateOpponentChoice(gameData, permanentId, demonstrate);
        } else if (context instanceof PermanentChoiceContext.SoulbondChoosePartner soulbondChoose) {
            battlefieldHandler.handleSoulbondChoosePartner(gameData, permanentId, soulbondChoose);
        } else if (context instanceof PermanentChoiceContext.ChampionedTriggerTarget championedTrigger) {
            triggerHandler.handleChampionedTrigger(gameData, permanentId, championedTrigger);
        } else if (context instanceof PermanentChoiceContext.SpellRetarget retarget) {
            spellHandler.handleSpellRetarget(gameData, permanentId, retarget);
        } else if (context instanceof PermanentChoiceContext.RedHerringExchange exchange) {
            redHerringExchangeEffectHandler.completeChoice(gameData, permanentId, exchange);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.PsychicBattleRetarget retarget) {
            spellHandler.handlePsychicBattleRetarget(gameData, permanentId, retarget);
        } else if (context instanceof PermanentChoiceContext.SpellTargetTriggerAnyTarget stt) {
            triggerHandler.handleSpellTargetTrigger(gameData, permanentId, stt);
        } else if (context instanceof PermanentChoiceContext.DiscardTriggerAnyTarget dtt) {
            triggerHandler.handleDiscardTrigger(gameData, permanentId, dtt);
        } else if (context instanceof PermanentChoiceContext.PlotTriggerAnyTarget plotTrigger) {
            triggerHandler.handlePlotTrigger(gameData, permanentId, plotTrigger);
        } else if (context instanceof PermanentChoiceContext.CopyPermanentTargetedBySpell) {
            triggerHandler.handleCopyPermanentTargetedBySpell(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.DiscardControllerTriggerTarget dct) {
            triggerHandler.handleDiscardControllerTrigger(gameData, permanentId, dct);
        } else if (context instanceof PermanentChoiceContext.DeathTriggerTarget dtt) {
            triggerHandler.handleDeathTrigger(gameData, permanentId, dtt);
        } else if (context instanceof PermanentChoiceContext.SelfTriggeredAbilityTarget slt) {
            triggerHandler.handleSelfTriggeredAbility(gameData, permanentId, slt);
        } else if (context instanceof PermanentChoiceContext.PreventDamageSourceChoice preventSource) {
            battlefieldHandler.handlePreventDamageSourceChoice(gameData, permanentId, preventSource);
        } else if (context instanceof PermanentChoiceContext.GuardDogsPermanentChoice) {
            battlefieldHandler.handleGuardDogsPermanentChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.RedirectDamageSourceChoice redirectSource) {
            battlefieldHandler.handleRedirectDamageSourceChoice(gameData, permanentId, redirectSource);
        } else if (context instanceof PermanentChoiceContext.RedirectCreatureDamageSourceChoice redirectCreatureSource) {
            battlefieldHandler.handleRedirectCreatureDamageSourceChoice(gameData, permanentId, redirectCreatureSource);
        } else if (context instanceof PermanentChoiceContext.PreventDamageToTargetFromSourceChoice preventTargetSource) {
            battlefieldHandler.handlePreventDamageToTargetFromSourceChoice(gameData, permanentId, preventTargetSource);
        } else if (context instanceof PermanentChoiceContext.PreventNextDamageFromSourceChoice preventNextSource) {
            battlefieldHandler.handlePreventNextDamageFromSourceChoice(gameData, permanentId, preventNextSource);
        } else if (context instanceof PermanentChoiceContext.PreventNextCombatDamageFromUnblockedCreatureChoice forcefield) {
            battlefieldHandler.handlePreventNextCombatDamageFromUnblockedCreatureChoice(gameData, permanentId, forcefield);
        } else if (context instanceof PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice preventNextAnyTarget) {
            battlefieldHandler.handlePreventNextDamageFromSourceToAnyTargetChoice(gameData, permanentId, preventNextAnyTarget);
        } else if (context instanceof PermanentChoiceContext.DoubleOrPreventNextDamageFromSourceChoice doubleOrPrevent) {
            battlefieldHandler.handleDoubleOrPreventNextDamageFromSourceChoice(gameData, permanentId, doubleOrPrevent);
        } else if (context instanceof PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice preventNextPermanent) {
            battlefieldHandler.handlePreventNextDamageFromSourceToPermanentChoice(gameData, permanentId, preventNextPermanent);
        } else if (context instanceof PermanentChoiceContext.PreventNextDamageFromSourceToYouAndYourCreaturesChoice preventNextYouAndCreatures) {
            battlefieldHandler.handlePreventNextDamageFromSourceToYouAndYourCreaturesChoice(gameData, permanentId, preventNextYouAndCreatures);
        } else if (context instanceof PermanentChoiceContext.EyeForAnEyeSourceChoice eyeForAnEye) {
            battlefieldHandler.handleEyeForAnEyeSourceChoice(gameData, permanentId, eyeForAnEye);
        } else if (context instanceof PermanentChoiceContext.RedirectNextDamageFromChosenSourceToPermanentChoice redirectToPermanent) {
            battlefieldHandler.handleRedirectNextDamageFromChosenSourceToPermanentChoice(gameData, permanentId, redirectToPermanent);
        } else if (context instanceof PermanentChoiceContext.RedirectPlayerDamageSourceChoice redirectToPlayer) {
            battlefieldHandler.handleRedirectPlayerDamageSourceChoice(gameData, permanentId, redirectToPlayer);
        } else if (context instanceof PermanentChoiceContext.ReflectDamageToSourceControllerChoice reflectDamage) {
            battlefieldHandler.handleReflectDamageToSourceControllerChoice(gameData, permanentId, reflectDamage);
        } else if (context instanceof PermanentChoiceContext.MayAbilityTriggerTarget mat) {
            triggerHandler.handleMayAbilityTrigger(gameData, permanentId, mat);
        } else if (context instanceof PermanentChoiceContext.RandomOpponentDamageChoice randomDamage) {
            triggerHandler.handleRandomOpponentDamageChoice(gameData, permanentId, randomDamage);
        } else if (context instanceof PermanentChoiceContext.EarthbendThenFightTarget earthbendFight) {
            earthbendThenFightHandler.handleTargetChoice(gameData, permanentId, earthbendFight);
        } else if (context instanceof PermanentChoiceContext.ResolvingModalTarget rmt) {
            triggerHandler.handleResolvingModalTarget(gameData, permanentId, rmt);
        } else if (context instanceof PermanentChoiceContext.AnyOpponentMayTapCreatureForToken aotc) {
            battlefieldHandler.handleAnyOpponentMayTapCreatureForToken(gameData, permanentId, aotc);
        } else if (context instanceof PermanentChoiceContext.ExploitTriggerTarget exploitTt) {
            triggerHandler.handleExploitTrigger(gameData, permanentId, exploitTt);
        } else if (context instanceof PermanentChoiceContext.ExploitPermanentTriggerTarget exploitPt) {
            triggerHandler.handleExploitPermanentTrigger(gameData, permanentId, exploitPt);
        } else if (context instanceof PermanentChoiceContext.ExploitPlayerTriggerTarget exploitPl) {
            triggerHandler.handleExploitPlayerTrigger(gameData, permanentId, exploitPl);
        } else if (context instanceof PermanentChoiceContext.LibraryCastSpellTarget lct) {
            spellHandler.handleLibraryCastSpellTarget(gameData, permanentId, lct);
        } else if (context instanceof PermanentChoiceContext.FreeCastSacrificeCost sacrificeCost) {
            exileFreeCastQueueSupport.completeSacrificeCost(gameData, permanentId, sacrificeCost);
        } else if (context instanceof PermanentChoiceContext.FreeCastBeholdCost beholdCost) {
            exileFreeCastQueueSupport.completeBeholdCost(gameData, permanentId, beholdCost);
        } else if (context instanceof PermanentChoiceContext.SplitFreeCastTarget splitTarget) {
            splitCardCastSupport.handleTarget(gameData, permanentId, splitTarget);
        } else if (context instanceof PermanentChoiceContext.ExileCastSpellTarget ect) {
            spellHandler.handleExileCastSpellTarget(gameData, permanentId, ect);
        } else if (context instanceof PermanentChoiceContext.ChandraTorchCastSpellTarget ctc) {
            spellHandler.handleChandraTorchCastSpellTarget(gameData, permanentId, ctc);
        } else if (context instanceof PermanentChoiceContext.VaanCastSpellTarget vct) {
            spellHandler.handleVaanCastSpellTarget(gameData, permanentId, vct);
        } else if (context instanceof PermanentChoiceContext.GraveyardCastSpellTarget gct) {
            spellHandler.handleGraveyardCastSpellTarget(gameData, permanentId, gct);
        } else if (context instanceof PermanentChoiceContext.HandCastSpellTarget hct) {
            spellHandler.handleHandCastSpellTarget(gameData, permanentId, hct);
        } else if (context instanceof PermanentChoiceContext.OpponentChosenSpellTarget opponentTarget) {
            spellHandler.handleOpponentChosenSpellTarget(gameData, permanentId, opponentTarget);
        } else if (context instanceof PermanentChoiceContext.OpponentChosenSpellTargets opponentTargets) {
            spellHandler.handleOpponentChosenSpellTargets(gameData, permanentId, opponentTargets);
        } else if (context instanceof PermanentChoiceContext.AttackTriggerTarget att) {
            triggerHandler.handleAttackTrigger(gameData, permanentId, att);
        } else if (context instanceof PermanentChoiceContext.CreateTokensAttacking createTokens) {
            triggerHandler.handleCreateTokensAttacking(gameData, permanentId, createTokens);
        } else if (context instanceof PermanentChoiceContext.CreateTokenCopiesAttacking tokenCopies) {
            triggerHandler.handleCreateTokenCopiesAttacking(gameData, permanentId, tokenCopies);
        } else if (context instanceof PermanentChoiceContext.TokenAttackTargetChoice tokenAttack) {
            triggerHandler.handleTokenAttackTargetChoice(gameData, permanentId, tokenAttack);
        } else if (context instanceof PermanentChoiceContext.PreparedTokenCopyAttachments tokenCopies) {
            triggerHandler.handlePreparedTokenCopyAttachment(gameData, permanentId, tokenCopies);
        } else if (context instanceof PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking tokenCopies) {
            triggerHandler.handlePreparedOpponentTokenCopiesAttacking(gameData, permanentId, tokenCopies);
        } else if (context instanceof PermanentChoiceContext.CreateTokenCopiesOfEnteredThisTurnAttacking tokenCopies) {
            triggerHandler.handleCreateTokenCopiesOfEnteredThisTurnAttacking(gameData, permanentId, tokenCopies);
        } else if (context instanceof PermanentChoiceContext.CreateMemoryCounterTokenCopiesAttacking tokenCopies) {
            triggerHandler.handleCreateMemoryCounterTokenCopiesAttacking(gameData, permanentId, tokenCopies);
        } else if (context instanceof PermanentChoiceContext.RevealUntilCardPredicateAttackTarget revealAttack) {
            triggerHandler.handleRevealUntilCardPredicateAttackTarget(gameData, permanentId, revealAttack);
        } else if (context instanceof PermanentChoiceContext.ChosenPermanentAttackTarget chosenAttackTarget) {
            triggerHandler.handleChosenPermanentAttackTarget(gameData, permanentId, chosenAttackTarget);
        } else if (context instanceof PermanentChoiceContext.ReselectAttackingCreatureTarget reselectAttackTarget) {
            triggerHandler.handleReselectAttackingCreatureTarget(gameData, permanentId, reselectAttackTarget);
        } else if (context instanceof PermanentChoiceContext.ReselectAttackTarget reselectAttackTarget) {
            triggerHandler.handleReselectedAttackTarget(gameData, permanentId, reselectAttackTarget);
        } else if (context instanceof PermanentChoiceContext.ExileReturnAttackTarget erat) {
            triggerHandler.handleExileReturnAttackTarget(gameData, permanentId, erat);
        } else if (context instanceof PermanentChoiceContext.EntersTriggerTarget ett) {
            triggerHandler.handleEntersTrigger(gameData, permanentId, ett);
        } else if (context instanceof PermanentChoiceContext.StackedModalTriggerTarget stackedModalTarget) {
            stackedModalTriggerService.handleTargetChosen(gameData, permanentId, stackedModalTarget);
        } else if (context instanceof PermanentChoiceContext.EmblemTriggerTarget ett) {
            triggerHandler.handleEmblemTrigger(gameData, permanentId, ett);
        } else if (context instanceof PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger umpt) {
            triggerHandler.handleUpkeepMultiPlayerFirstTarget(gameData, permanentId, umpt);
        } else if (context instanceof PermanentChoiceContext.UpkeepSecondPlayerTargetTrigger uspt) {
            triggerHandler.handleUpkeepMultiPlayerSecondTarget(gameData, permanentId, uspt);
        } else if (context instanceof PermanentChoiceContext.UpkeepAnyTargetTrigger uat) {
            triggerHandler.handleUpkeepAnyTargetTrigger(gameData, permanentId, uat);
        } else if (context instanceof PermanentChoiceContext.UpkeepPermanentTargetTrigger uptt) {
            triggerHandler.handleUpkeepPermanentTargetTrigger(gameData, permanentId, uptt);
        } else if (context instanceof PermanentChoiceContext.CyclopeanTombUpkeepLandChoice cyclopean) {
            removeAllMireCountersFromChosenLandEffectHandler.completeChoice(gameData, permanentId, cyclopean);
        } else if (context instanceof PermanentChoiceContext.DayNightTransformAttachment dnta) {
            triggerHandler.handleDayNightTransformAttachment(gameData, permanentId, dnta);
        } else if (context instanceof PermanentChoiceContext.DayNightTriggerTarget dntt) {
            triggerHandler.handleDayNightTriggerTarget(gameData, permanentId, dntt);
        } else if (context instanceof PermanentChoiceContext.PhasesInTriggerTarget pit) {
            triggerHandler.handlePhasesInTriggerTarget(gameData, permanentId, pit);
        } else if (context instanceof PermanentChoiceContext.UpkeepPlayerTargetTrigger upt) {
            triggerHandler.handleUpkeepPlayerTargetTrigger(gameData, permanentId, upt);
        } else if (context instanceof PermanentChoiceContext.MainPhasePlayerTargetTrigger mpt) {
            triggerHandler.handleMainPhasePlayerTargetTrigger(gameData, permanentId, mpt);
        } else if (context instanceof PermanentChoiceContext.PlayerWithLowestLifeChoice pwll) {
            triggerHandler.handlePlayerWithLowestLifeChoice(gameData, permanentId, pwll);
        } else if (context instanceof PermanentChoiceContext.BlackGateMostLifeChoice bgml) {
            triggerHandler.handleBlackGateMostLifeChoice(gameData, permanentId, bgml);
        } else if (context instanceof PermanentChoiceContext.TheMasterMostLifeChoice tmml) {
            triggerHandler.handleTheMasterMostLifeChoice(gameData, permanentId, tmml);
        } else if (context instanceof PermanentChoiceContext.LeastToughnessDamageChoice ltdc) {
            triggerHandler.handleLeastToughnessDamageChoice(gameData, permanentId, ltdc);
        } else if (context instanceof PermanentChoiceContext.UpkeepCopyTriggerTarget uct) {
            triggerHandler.handleUpkeepCopyTrigger(gameData, permanentId, uct);
        } else if (context instanceof PermanentChoiceContext.CapriciousEfreetOwnTarget ceo) {
            triggerHandler.handleCapriciousEfreetOwnTarget(gameData, permanentId, ceo);
        } else if (context instanceof PermanentChoiceContext.PucasMischiefOwnTarget pmot) {
            triggerHandler.handlePucasMischiefOwnTarget(gameData, permanentId, pmot);
        } else if (context instanceof PermanentChoiceContext.PucasMischiefOpponentTarget pmot) {
            triggerHandler.handlePucasMischiefOpponentTarget(gameData, permanentId, pmot);
        } else if (context instanceof PermanentChoiceContext.LifeGainTriggerAnyTarget lgt) {
            triggerHandler.handleLifeGainTrigger(gameData, permanentId, lgt);
        } else if (context instanceof PermanentChoiceContext.DrawTriggerAnyTarget dt) {
            triggerHandler.handleDrawTrigger(gameData, permanentId, dt);
        } else if (context instanceof PermanentChoiceContext.DrawTriggerPermanentTarget dpt) {
            triggerHandler.handleDrawTriggerPermanentTarget(gameData, permanentId, dpt);
        } else if (context instanceof PermanentChoiceContext.EnteringPermanentAnyTargetTrigger efg) {
            triggerHandler.handleEnteringPermanentAnyTarget(gameData, permanentId, efg);
        } else if (context instanceof PermanentChoiceContext.ETBSpellTargetTrigger etbStt) {
            triggerHandler.handleETBSpellTargetTrigger(gameData, permanentId, etbStt);
        } else if (context instanceof PermanentChoiceContext.ETBTokenTargetTrigger etbTtt) {
            triggerHandler.handleETBTokenTargetTrigger(gameData, permanentId, etbTtt);
        } else if (context instanceof PermanentChoiceContext.ETBTokenMultiTargetTrigger etbMtt) {
            triggerHandler.handleETBTokenMultiTargetTrigger(gameData, permanentId, etbMtt);
        } else if (context instanceof PermanentChoiceContext.EndStepTriggerTarget est) {
            triggerHandler.handleEndStepTrigger(gameData, permanentId, est);
        } else if (context instanceof PermanentChoiceContext.BeginningOfCombatTriggerTarget boct) {
            triggerHandler.handleBeginningOfCombatTrigger(gameData, permanentId, boct);
        } else if (context instanceof PermanentChoiceContext.ExploreTriggerTarget ett) {
            triggerHandler.handleExploreTrigger(gameData, permanentId, ett);
        } else if (context instanceof PermanentChoiceContext.ClashTriggerTarget ctt) {
            triggerHandler.handleClashTrigger(gameData, permanentId, ctt);
        } else if (context instanceof PermanentChoiceContext.TransformOpponentThenCreatureTarget tot) {
            triggerHandler.handleTransformOpponentTarget(gameData, permanentId, tot);
        } else if (context instanceof PermanentChoiceContext.TransformCreatureTarget tct) {
            triggerHandler.handleTransformCreatureTarget(gameData, permanentId, tct);
        } else if (context instanceof PermanentChoiceContext.TransformTriggerTarget ttt) {
            triggerHandler.handleTransformTriggerTarget(gameData, permanentId, ttt);
        } else if (context instanceof PermanentChoiceContext.AttackCounterMoveFirstTarget acmf) {
            triggerHandler.handleAttackCounterMoveFirstTarget(gameData, permanentId, acmf);
        } else if (context instanceof PermanentChoiceContext.AttackCounterMoveSecondTarget acms) {
            triggerHandler.handleAttackCounterMoveSecondTarget(gameData, permanentId, acms);
        } else if (context instanceof PermanentChoiceContext.SagaChapterTarget sct) {
            triggerHandler.handleSagaChapterTarget(gameData, permanentId, sct);
        } else if (context instanceof PermanentChoiceContext.SagaChapterPlayerTarget scpt) {
            triggerHandler.handleSagaChapterPlayerTarget(gameData, permanentId, scpt);
        } else if (context instanceof PermanentChoiceContext.TariffTieBreak tariffTieBreak) {
            battlefieldHandler.handleTariffTieBreak(gameData, permanentId, tariffTieBreak);
        } else if (context instanceof PermanentChoiceContext.DispersalTieBreak dispersalTieBreak) {
            dispersalEffectHandler.handleTieBreakChosen(gameData, permanentId, dispersalTieBreak);
        } else if (context instanceof PermanentChoiceContext.JuxtaposeTieBreak juxtaposeTieBreak) {
            battlefieldHandler.handleJuxtaposeTieBreak(gameData, permanentId, juxtaposeTieBreak);
        } else if (context instanceof PermanentChoiceContext.ChooseCreatureAsEnter ccae) {
            battlefieldHandler.handleChooseCreatureAsEnter(gameData, permanentId, ccae);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentPermanentAsEnter opa) {
            battlefieldHandler.handleChooseOpponentPermanentAsEnter(gameData, permanentId, opa);
        } else if (context instanceof PermanentChoiceContext.ChoosePlayerAsEnter cpae) {
            battlefieldHandler.handleChoosePlayerAsEnter(gameData, permanentId, cpae);
        } else if (context instanceof PermanentChoiceContext.ChooseTwoPlayersAsEnter ctpae) {
            battlefieldHandler.handleChooseTwoPlayersAsEnter(gameData, permanentId, ctpae);
        } else if (context instanceof PermanentChoiceContext.ChooseNonlandPermanentAsEnter ncpae) {
            battlefieldHandler.handleChooseNonlandPermanentAsEnter(gameData, permanentId, ncpae);
        } else if (context instanceof PermanentChoiceContext.ChooseEquipmentToAttachAsEnter equipmentChoice) {
            battlefieldHandler.handleChooseEquipmentToAttachAsEnter(gameData, permanentId, equipmentChoice);
        } else if (context instanceof PermanentChoiceContext.BlightCreatureChoice blight) {
            battlefieldHandler.handleBlightCreatureChoice(gameData, permanentId, blight);
        } else if (context instanceof PermanentChoiceContext.CounterUnlessBlightsCreatureChoice blight) {
            battlefieldHandler.handleCounterUnlessBlightsCreatureChoice(gameData, permanentId, blight);
        } else if (context instanceof PermanentChoiceContext.EachOpponentBlightsCreature blight) {
            battlefieldHandler.handleEachOpponentBlightsCreature(gameData, permanentId, blight);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToDestroy destroy) {
            battlefieldHandler.handleEachOpponentChoosesGreatestPowerCreatureToDestroy(gameData, permanentId, destroy);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesPermanentToDestroy destroy) {
            battlefieldHandler.handleEachOpponentChoosesPermanentToDestroy(gameData, permanentId, destroy);
        } else if (context instanceof PermanentChoiceContext.EachOpponentChoosesGreatestManaValueCreatureToReturnToHand returnToHand) {
            battlefieldHandler.handleEachOpponentChoosesGreatestManaValueCreatureToReturnToHand(gameData,
                    permanentId, returnToHand);
        } else if (context instanceof PermanentChoiceContext.EachPlayerChoosesCreatureToDestroy destroy) {
            battlefieldHandler.handleEachPlayerChoosesCreatureToDestroy(gameData, permanentId, destroy);
        } else if (context instanceof PermanentChoiceContext.ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom destroy) {
            battlefieldHandler.handleChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom(
                    gameData, permanentId, destroy);
        } else if (context instanceof PermanentChoiceContext.EachOpponentCreatesTokenUnlessSacrificesCreature sacrificeOrToken) {
            battlefieldHandler.handleEachOpponentCreatesTokenUnlessSacrificesCreature(gameData, permanentId,
                    sacrificeOrToken);
        } else if (context instanceof PermanentChoiceContext.ManaAbilityAddToChosenPlayer manaChosen) {
            battlefieldHandler.handleManaAbilityAddToChosenPlayer(gameData, permanentId, manaChosen);
        } else if (context instanceof PermanentChoiceContext.BendOrBreakOpponentChoice) {
            bendOrBreakEffectHandler.completeOpponentChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.OpponentChoosesCardFromGraveyardToHand) {
            opponentChoosesCardHandler.completeOpponentChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.DawnbreakReclaimerOpponentChoice) {
            dawnbreakReclaimerEffectHandler.completeOpponentChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.CuratorOpponentChoice) {
            battlefieldHandler.handleCuratorOpponentChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.ChooseOwnCreatureGrantKeyword grantKeyword) {
            grantChosenCreatureKeywordEffectHandler.completeChoice(gameData, permanentId, grantKeyword);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.KathrilKeywordCounterChoice kathrilChoice) {
            kathrilEffectHandler.completeChoice(gameData, permanentId, kathrilChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.RingBearerChoice ringBearerChoice) {
            ringTemptsYouEffectHandler.completeChoice(gameData, permanentId, ringBearerChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.TemptTheRingBearerChoice ringBearerChoice) {
            temptTheRingEffectHandler.completeChoice(gameData, permanentId, ringBearerChoice);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.SuspectChosenOtherCreature) {
            suspectChosenOtherCreatureEffectHandler.completeChoice(gameData, permanentId);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (context instanceof PermanentChoiceContext.ContractualSafeguardReference) {
            contractualSafeguardEffectHandler.completeReferenceChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.ChooseCounterTypeOnControlledPermanentReference) {
            avenCourierEffectHandler.completeReferenceChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.RemoveCounterFromChosenOwnPermanent removeCounter) {
            removeCounterFromChosenOwnPermanentEffectHandler.completeChoice(gameData, permanentId, removeCounter);
        } else if (context instanceof PermanentChoiceContext.TurnOwnCreatureFaceUp faceUp) {
            turnOwnCreatureFaceUpEffectHandler.completeChoice(gameData, permanentId, faceUp.creaturesOnly());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        } else if (gameData.interaction.pendingAuraCard() != null) {
            battlefieldHandler.handlePendingAuraPlacement(gameData, playerId, permanentId);
        } else {
            throw new IllegalStateException("No pending permanent choice context");
        }
    }

}
