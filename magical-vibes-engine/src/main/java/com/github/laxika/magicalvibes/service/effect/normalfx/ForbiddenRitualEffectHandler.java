package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.ForbiddenRitualState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForbiddenRitualEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ForbiddenRitualEffect}: the controller sacrifices a nontoken permanent; if they
 * do, the targeted opponent loses N life unless they sacrifice a permanent or discard a card; then
 * the controller may repeat against the same opponent.
 *
 * <p>Progress lives on {@link GameData#forbiddenRitual}. Controller sacrifice and opponent
 * sacrifice/discard sub-choices re-run this handler via
 * {@link GameData#rerunCurrentEffectAfterInteraction}. The optional repeat is a
 * {@link PendingInteraction.ForbiddenRitualRepeatChoice} (handled by
 * {@code ForbiddenRitualRepeatChoiceInteractionHandler}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForbiddenRitualEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForbiddenRitualEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int lifeLoss = ((ForbiddenRitualEffect) effect).lifeLoss();
        ForbiddenRitualState state = gameData.forbiddenRitual;
        String sourceName = entry.getCard().getName();
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();

        if (opponentId == null || !gameData.playerIds.contains(opponentId)) {
            finish(gameData, state);
            return;
        }

        if (!state.active) {
            state.reset();
            state.active = true;
            state.lifeLoss = lifeLoss;
            beginControllerSacrifice(gameData, entry, sourceName, controllerId, opponentId, lifeLoss);
            return;
        }

        if (state.chosenMode != null) {
            String mode = state.chosenMode;
            state.chosenMode = null;
            applyOpponentMode(gameData, entry, sourceName, controllerId, opponentId, lifeLoss, mode);
            return;
        }

        if (!state.controllerSacrificed) {
            // Re-entry after the controller's sacrifice choice completed.
            state.controllerSacrificed = true;
            offerOpponentPenalty(gameData, entry, sourceName, controllerId, opponentId, lifeLoss);
            return;
        }

        // Re-entry after the opponent's sacrifice/discard sub-choice — offer the optional repeat.
        offerRepeatOrFinish(gameData, entry, sourceName, controllerId);
    }

    /**
     * Starts (or restarts after an accepted repeat) the controller's nontoken-permanent sacrifice.
     * No legal permanent ends the process; a single match is sacrificed without a prompt.
     */
    public void beginControllerSacrifice(GameData gameData, StackEntry entry, String sourceName,
            UUID controllerId, UUID opponentId, int lifeLoss) {
        ForbiddenRitualState state = gameData.forbiddenRitual;
        state.controllerSacrificed = false;
        state.chosenMode = null;

        List<UUID> nontokenIds = nontokenPermanentIds(gameData, controllerId);
        if (nontokenIds.isEmpty()) {
            finish(gameData, state);
            return;
        }

        if (nontokenIds.size() == 1) {
            Permanent only = gameQueryService.findPermanentById(gameData, nontokenIds.getFirst());
            if (only != null) {
                destructionSupport.sacrificeAndLog(gameData, only, controllerId);
            }
            state.controllerSacrificed = true;
            offerOpponentPenalty(gameData, entry, sourceName, controllerId, opponentId, lifeLoss);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, nontokenIds,
                sourceName + " — sacrifice a nontoken permanent.");
    }

    private void offerOpponentPenalty(GameData gameData, StackEntry entry, String sourceName,
            UUID controllerId, UUID opponentId, int lifeLoss) {
        if (!gameData.playerIds.contains(opponentId)) {
            finish(gameData, gameData.forbiddenRitual);
            return;
        }

        List<String> options = availableOpponentOptions(gameData, opponentId, lifeLoss);
        if (options.size() == 1) {
            lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
            gameOutcomeService.checkWinCondition(gameData);
            offerRepeatOrFinish(gameData, entry, sourceName, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        String prompt = sourceName + " — lose " + lifeLoss
                + " life unless you sacrifice a permanent or discard a card.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                opponentId, null, null,
                new ChoiceContext.ForbiddenRitualPenaltyChoice(opponentId, sourceName),
                options, prompt));
    }

    private void applyOpponentMode(GameData gameData, StackEntry entry, String sourceName,
            UUID controllerId, UUID opponentId, int lifeLoss, String mode) {
        if (ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE.equals(mode)) {
            List<UUID> permanentIds = anyPermanentIds(gameData, opponentId);
            if (permanentIds.isEmpty()) {
                offerRepeatOrFinish(gameData, entry, sourceName, controllerId);
                return;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.TormentSacrifice(opponentId));
            playerInputService.beginPermanentChoice(gameData, opponentId, permanentIds,
                    sourceName + " — choose a permanent to sacrifice.");
            return;
        }

        if (ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD.equals(mode)) {
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()) {
                offerRepeatOrFinish(gameData, entry, sourceName, controllerId);
                return;
            }
            gameData.discardCausedByOpponent = !opponentId.equals(controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, opponentId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                offerRepeatOrFinish(gameData, entry, sourceName, controllerId);
            }
            return;
        }

        lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
        gameOutcomeService.checkWinCondition(gameData);
        offerRepeatOrFinish(gameData, entry, sourceName, controllerId);
    }

    private void offerRepeatOrFinish(GameData gameData, StackEntry entry, String sourceName,
            UUID controllerId) {
        ForbiddenRitualState state = gameData.forbiddenRitual;
        if (nontokenPermanentIds(gameData, controllerId).isEmpty()) {
            finish(gameData, state);
            return;
        }
        // Parked spell waits on the accept/decline; decline (or a finished accept path) resumes it.
        gameData.rerunCurrentEffectAfterInteraction = false;
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ForbiddenRitualRepeatChoice(controllerId, sourceName));
    }

    public void finish(GameData gameData, ForbiddenRitualState state) {
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private List<String> availableOpponentOptions(GameData gameData, UUID opponentId, int lifeLoss) {
        List<String> options = new ArrayList<>();
        if (!anyPermanentIds(gameData, opponentId).isEmpty()) {
            options.add(ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE);
        }
        List<Card> hand = gameData.playerHands.get(opponentId);
        if (hand != null && !hand.isEmpty()) {
            options.add(ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD);
        }
        options.add("Lose " + lifeLoss + " life");
        return options;
    }

    private List<UUID> nontokenPermanentIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                p -> !p.getCard().isToken());
    }

    private List<UUID> anyPermanentIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId, p -> true);
    }
}
