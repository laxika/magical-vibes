package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BendOrBreakEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MurmursFromBeyondEffectHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Dispatcher for permanent choice inputs. Validates the incoming choice
 * and delegates to the appropriate thematic handler service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceHandlerService {

    private final PermanentChoiceTriggerHandlerService triggerHandler;
    private final PermanentChoiceSpellHandlerService spellHandler;
    private final PermanentChoiceBattlefieldHandlerService battlefieldHandler;
    private final AbilityActivationService abilityActivationService;
    private final BendOrBreakEffectHandler bendOrBreakEffectHandler;
    private final EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffectHandler dispersalEffectHandler;
    private final MurmursFromBeyondEffectHandler murmursFromBeyondEffectHandler;

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
            throw new IllegalStateException("Invalid permanent: " + permanentId);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearPermanentChoiceContext();

        PermanentChoiceContext context = permanentChoice.context();

        if (context instanceof PermanentChoiceContext.CloneCopy) {
            battlefieldHandler.handleCloneCopy(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.CipherEncode) {
            battlefieldHandler.handleCipherEncode(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.AttachEquipmentToCreature attachEquip) {
            battlefieldHandler.handleAttachEquipmentToCreature(gameData, permanentId, attachEquip);
        } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
            battlefieldHandler.handleAuraGraft(gameData, permanentId, auraGraft);
        } else if (context instanceof PermanentChoiceContext.AttachAllAurasToAnotherPermanent attachAll) {
            battlefieldHandler.handleAttachAllAurasToAnotherPermanent(gameData, permanentId, attachAll);
        } else if (context instanceof PermanentChoiceContext.ReattachSourceAuraAfterSacrifice reattach) {
            battlefieldHandler.handleReattachSourceAuraAfterSacrifice(gameData, permanentId, reattach);
        } else if (context instanceof PermanentChoiceContext.AttachSourceAuraToChosenPermanent attachAura) {
            battlefieldHandler.handleAttachSourceAuraToChosenPermanent(gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.AttachTargetAuraToAnotherPermanentOfSameType attachAura) {
            battlefieldHandler.handleAttachTargetAuraToAnotherPermanentOfSameType(gameData, permanentId, attachAura);
        } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
            battlefieldHandler.handleLegendRule(gameData, playerId, permanentId, legendRule);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife sacrificeOpp) {
            battlefieldHandler.handleSacrificeCreatureOpponentsLoseLife(gameData, permanentId, sacrificeOpp);
        } else if (context instanceof PermanentChoiceContext.ForcedCostOrElse forcedCostOrElse) {
            battlefieldHandler.handleForcedCostOrElse(gameData, permanentId, forcedCostOrElse);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness sacrificeGainLife) {
            battlefieldHandler.handleSacrificeCreatureControllerGainsLifeEqualToToughness(gameData, permanentId, sacrificeGainLife);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureThenSearchLibrary sacrificeSearch) {
            battlefieldHandler.handleSacrificeCreatureThenSearchLibrary(gameData, permanentId, sacrificeSearch);
        } else if (context instanceof PermanentChoiceContext.SacrificeOneOfTwoThenCounterOnOther sacrificeOneOfTwo) {
            battlefieldHandler.handleSacrificeOneOfTwoThenCounterOnOther(gameData, permanentId, sacrificeOneOfTwo);
        } else if (context instanceof PermanentChoiceContext.CannibalizeChoice cannibalize) {
            battlefieldHandler.handleCannibalizeChoice(gameData, permanentId, cannibalize);
        } else if (context instanceof PermanentChoiceContext.SacrificeOneOfTwoThenReturnOtherToHand sacrificeOneOfTwo) {
            battlefieldHandler.handleSacrificeOneOfTwoThenReturnOtherToHand(gameData, permanentId, sacrificeOneOfTwo);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
            battlefieldHandler.handleSacrificeCreature(gameData, permanentId, sacrificeCreature);
        } else if (context instanceof PermanentChoiceContext.TormentSacrifice tormentSacrifice) {
            battlefieldHandler.handleTormentSacrifice(gameData, permanentId, tormentSacrifice);
        } else if (context instanceof PermanentChoiceContext.DestroyChosenCreature destroyChosenCreature) {
            battlefieldHandler.handleDestroyChosenCreature(gameData, permanentId, destroyChosenCreature);
        } else if (context instanceof PermanentChoiceContext.ExileCombatOpponent exileCombatOpponent) {
            battlefieldHandler.handleExileCombatOpponent(gameData, permanentId, exileCombatOpponent);
        } else if (context instanceof PermanentChoiceContext.DefendingPlayerChoosesCreatureToBlock chooseBlocker) {
            battlefieldHandler.handleDefendingPlayerChoosesCreatureToBlock(gameData, permanentId, chooseBlocker);
        } else if (context instanceof PermanentChoiceContext.BalduvianWarlordChoosesAttacker chooseAttacker) {
            battlefieldHandler.handleBalduvianWarlordChoosesAttacker(gameData, permanentId, chooseAttacker);
        } else if (context instanceof PermanentChoiceContext.OpponentChoosesCreatureYouGainControl richesChoice) {
            battlefieldHandler.handleOpponentChoosesCreatureYouGainControl(gameData, permanentId, richesChoice);
        } else if (context instanceof PermanentChoiceContext.ChooseOpponentGainsControlOfSource chooseOpponent) {
            battlefieldHandler.handleChooseOpponentGainsControlOfSource(gameData, permanentId, chooseOpponent);
        } else if (context instanceof PermanentChoiceContext.MurmursFromBeyondOpponentChoice murmursChoice) {
            murmursFromBeyondEffectHandler.completeOpponentChoice(gameData, permanentId, murmursChoice);
        } else if (context instanceof PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy echoChamberChoice) {
            battlefieldHandler.handleOpponentChoosesCreatureTheyControlToCopy(gameData, permanentId, echoChamberChoice);
        } else if (context instanceof PermanentChoiceContext.OpponentMayGainControlOfCreatureYouControl opponentSteal) {
            battlefieldHandler.handleOpponentMayGainControlOfCreatureYouControl(gameData, permanentId, opponentSteal);
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
            battlefieldHandler.handleActivatedAbilityCostChoice(gameData, player, permanentId, costChoice);
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilityOpponentTarget opponentTarget) {
            abilityActivationService.handleOpponentChosenTarget(gameData, player, permanentId, opponentTarget);
        } else if (context instanceof PermanentChoiceContext.GraveyardAbilityCostChoice graveyardCostChoice) {
            battlefieldHandler.handleGraveyardAbilityCostChoice(gameData, player, permanentId, graveyardCostChoice);
        } else if (context instanceof PermanentChoiceContext.MayAbilityTapCostChoice mayTapCostChoice) {
            battlefieldHandler.handleMayAbilityTapCostChoice(gameData, player, permanentId, mayTapCostChoice);
        } else if (context instanceof PermanentChoiceContext.BounceCreature) {
            battlefieldHandler.handleBounceCreature(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.BouncePermanentThen bounceThen) {
            battlefieldHandler.handleBouncePermanentThen(gameData, permanentId, bounceThen);
        } else if (context instanceof PermanentChoiceContext.BounceOwnPermanentOrSacrificeSelf) {
            battlefieldHandler.handleBounceOwnPermanentOrSacrificeSelf(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.SacrificeOwnPermanentOrSacrificeSelf) {
            battlefieldHandler.handleSacrificeOwnPermanentOrSacrificeSelf(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.SacrificePermanentToEnter sacToEnter) {
            battlefieldHandler.handleSacrificePermanentToEnter(gameData, permanentId, sacToEnter);
        } else if (context instanceof PermanentChoiceContext.ChampionCreature championCreature) {
            battlefieldHandler.handleChampionCreature(gameData, permanentId, championCreature);
        } else if (context instanceof PermanentChoiceContext.Populate populate) {
            battlefieldHandler.handlePopulate(gameData, permanentId, populate);
        } else if (context instanceof PermanentChoiceContext.PutControlledCreatureOnTopOfLibrary putOnTop) {
            battlefieldHandler.handlePutControlledCreatureOnTopOfLibrary(gameData, permanentId, putOnTop);
        } else if (context instanceof PermanentChoiceContext.PatternMatcherCreatureChoice patternMatcher) {
            battlefieldHandler.handlePatternMatcherCreatureChoice(gameData, permanentId, patternMatcher);
        } else if (context instanceof PermanentChoiceContext.PolymorphousRushCreatureChoice polymorphousRush) {
            battlefieldHandler.handlePolymorphousRushCreatureChoice(gameData, permanentId, polymorphousRush);
        } else if (context instanceof PermanentChoiceContext.CopySpellForOtherControlledCreatureChoice copyChoice) {
            triggerHandler.handleCopySpellForOtherControlledCreature(gameData, permanentId, copyChoice);
        } else if (context instanceof PermanentChoiceContext.SoulbondChoosePartner soulbondChoose) {
            battlefieldHandler.handleSoulbondChoosePartner(gameData, permanentId, soulbondChoose);
        } else if (context instanceof PermanentChoiceContext.ChampionedTriggerTarget championedTrigger) {
            triggerHandler.handleChampionedTrigger(gameData, permanentId, championedTrigger);
        } else if (context instanceof PermanentChoiceContext.SpellRetarget retarget) {
            spellHandler.handleSpellRetarget(gameData, permanentId, retarget);
        } else if (context instanceof PermanentChoiceContext.PsychicBattleRetarget retarget) {
            spellHandler.handlePsychicBattleRetarget(gameData, permanentId, retarget);
        } else if (context instanceof PermanentChoiceContext.SpellTargetTriggerAnyTarget stt) {
            triggerHandler.handleSpellTargetTrigger(gameData, permanentId, stt);
        } else if (context instanceof PermanentChoiceContext.DiscardTriggerAnyTarget dtt) {
            triggerHandler.handleDiscardTrigger(gameData, permanentId, dtt);
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
        } else if (context instanceof PermanentChoiceContext.ResolvingModalTarget rmt) {
            triggerHandler.handleResolvingModalTarget(gameData, permanentId, rmt);
        } else if (context instanceof PermanentChoiceContext.MaySacrificeForCounterOnSource msfc) {
            battlefieldHandler.handleMaySacrificeForCounterOnSource(gameData, permanentId, msfc);
        } else if (context instanceof PermanentChoiceContext.GargantuanGorillaSacrificeForest ggsf) {
            battlefieldHandler.handleGargantuanGorillaSacrificeForest(gameData, permanentId, ggsf);
        } else if (context instanceof PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndCounter aosc) {
            battlefieldHandler.handleAnyOpponentSacrificeCreatureForTapAndCounter(gameData, permanentId, aosc);
        } else if (context instanceof PermanentChoiceContext.AnyPlayerMaySacrificeLandPutSourceOnTop ams) {
            battlefieldHandler.handleAnyPlayerMaySacrificeLandPutSourceOnTop(gameData, permanentId, ams);
        } else if (context instanceof PermanentChoiceContext.SacrificePermanentThen spt) {
            battlefieldHandler.handleSacrificePermanentThen(gameData, permanentId, spt);
        } else if (context instanceof PermanentChoiceContext.SacrificePermanentAndReturnTargetCards spar) {
            battlefieldHandler.handleSacrificePermanentAndReturnTargetCards(gameData, permanentId, spar);
        } else if (context instanceof PermanentChoiceContext.SacrificePermanentAndBoostSelf spabs) {
            battlefieldHandler.handleSacrificePermanentAndBoostSelf(gameData, permanentId, spabs);
        } else if (context instanceof PermanentChoiceContext.SacrificePermanentAndGrantKeywordSelf spagks) {
            battlefieldHandler.handleSacrificePermanentAndGrantKeywordSelf(gameData, permanentId, spagks);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureCreateTokensEqualToToughness scct) {
            battlefieldHandler.handleSacrificeCreatureCreateTokensEqualToToughness(gameData, permanentId, scct);
        } else if (context instanceof PermanentChoiceContext.SacrificeOtherCreatureThenRevealUntilLowerManaValue kethek) {
            battlefieldHandler.handleSacrificeOtherCreatureThenRevealUntilLowerManaValue(gameData, permanentId, kethek);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureCreateSizedTokenEqualToPower scsp) {
            battlefieldHandler.handleSacrificeCreatureCreateSizedTokenEqualToPower(gameData, permanentId, scsp);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureCreateTokensIfSubtype scts) {
            battlefieldHandler.handleSacrificeCreatureCreateTokensIfSubtype(gameData, permanentId, scts);
        } else if (context instanceof PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
            battlefieldHandler.handleSacrificeArtifactForDividedDamage(gameData, permanentId, sadd);
        } else if (context instanceof PermanentChoiceContext.SacrificeAnotherCreatureDealPowerDamage sacpd) {
            battlefieldHandler.handleSacrificeAnotherCreatureDealPowerDamage(gameData, permanentId, sacpd);
        } else if (context instanceof PermanentChoiceContext.SacrificeAnotherCreatureGainLifeAndDraw sacgld) {
            battlefieldHandler.handleSacrificeAnotherCreatureGainLifeAndDraw(gameData, permanentId, sacgld);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureThenMassDamageEqualToPower rupture) {
            battlefieldHandler.handleSacrificeCreatureThenMassDamageEqualToPower(gameData, permanentId, rupture);
        } else if (context instanceof PermanentChoiceContext.ExploitSacrifice exploitSac) {
            battlefieldHandler.handleExploitSacrifice(gameData, permanentId, exploitSac);
        } else if (context instanceof PermanentChoiceContext.ExploitTriggerTarget exploitTt) {
            triggerHandler.handleExploitTrigger(gameData, permanentId, exploitTt);
        } else if (context instanceof PermanentChoiceContext.LibraryCastSpellTarget lct) {
            spellHandler.handleLibraryCastSpellTarget(gameData, permanentId, lct);
        } else if (context instanceof PermanentChoiceContext.ExileCastSpellTarget ect) {
            spellHandler.handleExileCastSpellTarget(gameData, permanentId, ect);
        } else if (context instanceof PermanentChoiceContext.ChandraTorchCastSpellTarget ctc) {
            spellHandler.handleChandraTorchCastSpellTarget(gameData, permanentId, ctc);
        } else if (context instanceof PermanentChoiceContext.GraveyardCastSpellTarget gct) {
            spellHandler.handleGraveyardCastSpellTarget(gameData, permanentId, gct);
        } else if (context instanceof PermanentChoiceContext.HandCastSpellTarget hct) {
            spellHandler.handleHandCastSpellTarget(gameData, permanentId, hct);
        } else if (context instanceof PermanentChoiceContext.AttackTriggerTarget att) {
            triggerHandler.handleAttackTrigger(gameData, permanentId, att);
        } else if (context instanceof PermanentChoiceContext.CreateTokensAttacking createTokens) {
            triggerHandler.handleCreateTokensAttacking(gameData, permanentId, createTokens);
        } else if (context instanceof PermanentChoiceContext.ExileReturnAttackTarget erat) {
            triggerHandler.handleExileReturnAttackTarget(gameData, permanentId, erat);
        } else if (context instanceof PermanentChoiceContext.EntersTriggerTarget ett) {
            triggerHandler.handleEntersTrigger(gameData, permanentId, ett);
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
        } else if (context instanceof PermanentChoiceContext.PhasesInTriggerTarget pit) {
            triggerHandler.handlePhasesInTriggerTarget(gameData, permanentId, pit);
        } else if (context instanceof PermanentChoiceContext.UpkeepPlayerTargetTrigger upt) {
            triggerHandler.handleUpkeepPlayerTargetTrigger(gameData, permanentId, upt);
        } else if (context instanceof PermanentChoiceContext.MainPhasePlayerTargetTrigger mpt) {
            triggerHandler.handleMainPhasePlayerTargetTrigger(gameData, permanentId, mpt);
        } else if (context instanceof PermanentChoiceContext.PlayerWithLowestLifeChoice pwll) {
            triggerHandler.handlePlayerWithLowestLifeChoice(gameData, permanentId, pwll);
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
        } else if (context instanceof PermanentChoiceContext.TariffTieBreak tariffTieBreak) {
            battlefieldHandler.handleTariffTieBreak(gameData, permanentId, tariffTieBreak);
        } else if (context instanceof PermanentChoiceContext.DispersalTieBreak dispersalTieBreak) {
            dispersalEffectHandler.handleTieBreakChosen(gameData, permanentId, dispersalTieBreak);
        } else if (context instanceof PermanentChoiceContext.JuxtaposeTieBreak juxtaposeTieBreak) {
            battlefieldHandler.handleJuxtaposeTieBreak(gameData, permanentId, juxtaposeTieBreak);
        } else if (context instanceof PermanentChoiceContext.ChooseCreatureAsEnter ccae) {
            battlefieldHandler.handleChooseCreatureAsEnter(gameData, permanentId, ccae);
        } else if (context instanceof PermanentChoiceContext.BlightCreatureChoice blight) {
            battlefieldHandler.handleBlightCreatureChoice(gameData, permanentId, blight);
        } else if (context instanceof PermanentChoiceContext.EachOpponentBlightsCreature blight) {
            battlefieldHandler.handleEachOpponentBlightsCreature(gameData, permanentId, blight);
        } else if (context instanceof PermanentChoiceContext.ManaAbilityAddToChosenPlayer manaChosen) {
            battlefieldHandler.handleManaAbilityAddToChosenPlayer(gameData, permanentId, manaChosen);
        } else if (context instanceof PermanentChoiceContext.BendOrBreakOpponentChoice) {
            bendOrBreakEffectHandler.completeOpponentChoice(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.CuratorOpponentChoice) {
            battlefieldHandler.handleCuratorOpponentChoice(gameData, permanentId);
        } else if (gameData.interaction.pendingAuraCard() != null) {
            battlefieldHandler.handlePendingAuraPlacement(gameData, playerId, permanentId);
        } else {
            throw new IllegalStateException("No pending permanent choice context");
        }
    }

}
