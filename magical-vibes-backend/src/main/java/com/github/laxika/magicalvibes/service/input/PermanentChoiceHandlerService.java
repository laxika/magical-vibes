package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private final MultiPermanentChoiceHandlerService multiPermanentHandler;

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)) {
            throw new IllegalStateException("Not awaiting permanent choice");
        }
        InteractionContext.PermanentChoice permanentChoice = gameData.interaction.permanentChoiceContextView();
        if (permanentChoice == null || !player.getId().equals(permanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = permanentChoice.validIds();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearPermanentChoice();

        if (!validIds.contains(permanentId)) {
            throw new IllegalStateException("Invalid permanent: " + permanentId);
        }

        PermanentChoiceContext context = permanentChoice.context();
        gameData.interaction.clearPermanentChoiceContext();

        if (context instanceof PermanentChoiceContext.CloneCopy) {
            battlefieldHandler.handleCloneCopy(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
            battlefieldHandler.handleAuraGraft(gameData, permanentId, auraGraft);
        } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
            battlefieldHandler.handleLegendRule(gameData, playerId, permanentId, legendRule);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife sacrificeOpp) {
            battlefieldHandler.handleSacrificeCreatureOpponentsLoseLife(gameData, permanentId, sacrificeOpp);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness sacrificeGainLife) {
            battlefieldHandler.handleSacrificeCreatureControllerGainsLifeEqualToToughness(gameData, permanentId, sacrificeGainLife);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreatureThenSearchLibrary sacrificeSearch) {
            battlefieldHandler.handleSacrificeCreatureThenSearchLibrary(gameData, permanentId, sacrificeSearch);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
            battlefieldHandler.handleSacrificeCreature(gameData, permanentId, sacrificeCreature);
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
            battlefieldHandler.handleActivatedAbilityCostChoice(gameData, player, permanentId, costChoice);
        } else if (context instanceof PermanentChoiceContext.BounceCreature bounceCreature) {
            battlefieldHandler.handleBounceCreature(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.BounceOwnPermanentOrSacrificeSelf bounceOrSac) {
            battlefieldHandler.handleBounceOwnPermanentOrSacrificeSelf(gameData, permanentId);
        } else if (context instanceof PermanentChoiceContext.SpellRetarget retarget) {
            spellHandler.handleSpellRetarget(gameData, permanentId, retarget);
        } else if (context instanceof PermanentChoiceContext.SpellTargetTriggerAnyTarget stt) {
            triggerHandler.handleSpellTargetTrigger(gameData, permanentId, stt);
        } else if (context instanceof PermanentChoiceContext.DiscardTriggerAnyTarget dtt) {
            triggerHandler.handleDiscardTrigger(gameData, permanentId, dtt);
        } else if (context instanceof PermanentChoiceContext.DeathTriggerTarget dtt) {
            triggerHandler.handleDeathTrigger(gameData, permanentId, dtt);
        } else if (context instanceof PermanentChoiceContext.PreventDamageSourceChoice preventSource) {
            battlefieldHandler.handlePreventDamageSourceChoice(gameData, permanentId, preventSource);
        } else if (context instanceof PermanentChoiceContext.RedirectDamageSourceChoice redirectSource) {
            battlefieldHandler.handleRedirectDamageSourceChoice(gameData, permanentId, redirectSource);
        } else if (context instanceof PermanentChoiceContext.MayAbilityTriggerTarget mat) {
            triggerHandler.handleMayAbilityTrigger(gameData, permanentId, mat);
        } else if (context instanceof PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
            battlefieldHandler.handleSacrificeArtifactForDividedDamage(gameData, permanentId, sadd);
        } else if (context instanceof PermanentChoiceContext.LibraryCastSpellTarget lct) {
            spellHandler.handleLibraryCastSpellTarget(gameData, permanentId, lct);
        } else if (context instanceof PermanentChoiceContext.ExileCastSpellTarget ect) {
            spellHandler.handleExileCastSpellTarget(gameData, permanentId, ect);
        } else if (context instanceof PermanentChoiceContext.GraveyardCastSpellTarget gct) {
            spellHandler.handleGraveyardCastSpellTarget(gameData, permanentId, gct);
        } else if (context instanceof PermanentChoiceContext.HandCastSpellTarget hct) {
            spellHandler.handleHandCastSpellTarget(gameData, permanentId, hct);
        } else if (context instanceof PermanentChoiceContext.AttackTriggerTarget att) {
            triggerHandler.handleAttackTrigger(gameData, permanentId, att);
        } else if (context instanceof PermanentChoiceContext.EmblemTriggerTarget ett) {
            triggerHandler.handleEmblemTrigger(gameData, permanentId, ett);
        } else if (context instanceof PermanentChoiceContext.UpkeepPlayerTargetTrigger upt) {
            triggerHandler.handleUpkeepPlayerTargetTrigger(gameData, permanentId, upt);
        } else if (context instanceof PermanentChoiceContext.UpkeepCopyTriggerTarget uct) {
            triggerHandler.handleUpkeepCopyTrigger(gameData, permanentId, uct);
        } else if (context instanceof PermanentChoiceContext.CapriciousEfreetOwnTarget ceo) {
            triggerHandler.handleCapriciousEfreetOwnTarget(gameData, permanentId, ceo);
        } else if (context instanceof PermanentChoiceContext.EndStepTriggerTarget est) {
            triggerHandler.handleEndStepTrigger(gameData, permanentId, est);
        } else if (context instanceof PermanentChoiceContext.ChooseCreatureAsEnter ccae) {
            battlefieldHandler.handleChooseCreatureAsEnter(gameData, permanentId, ccae);
        } else if (gameData.interaction.pendingAuraCard() != null) {
            battlefieldHandler.handlePendingAuraPlacement(gameData, playerId, permanentId);
        } else {
            throw new IllegalStateException("No pending permanent choice context");
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        multiPermanentHandler.handleMultiplePermanentsChosen(gameData, player, permanentIds);
    }
}
