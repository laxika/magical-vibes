package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentTapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentChoiceCostHandler;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Pays {@link TapMultiplePermanentsCost} during resolution-time may abilities and resumes
 * stack effect resolution when the cost is fully paid.
 */
@Service
@RequiredArgsConstructor
public class MayAbilityTapCostService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final EffectResolutionService effectResolutionService;

    /**
     * Begins interactive tap-cost payment after the player accepted a may ability with a tap cost.
     *
     * @return {@code true} if awaiting player input; {@code false} if the cost was auto-paid
     */
    public boolean beginTapCostPayment(GameData gameData, Player player, TapMultiplePermanentsCost tapCost,
                                       UUID sourcePermanentId) {
        PermanentChoiceCostHandler handler = new MultiplePermanentTapCostHandler(
                tapCost, gameQueryService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        UUID playerId = player.getId();

        try {
            handler.validateCanPay(gameData, playerId);
        } catch (IllegalStateException e) {
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " cannot tap enough permanents to pay the cost.");
            gameData.resolvedMayAccepted = false;
            resumeEffectResolution(gameData);
            return false;
        }

        int required = handler.requiredCount();
        if (handler.shouldAutoPayAll(gameData, playerId, required)) {
            List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
            for (UUID id : validIds) {
                Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                if (chosen != null) {
                    handler.validateAndPay(gameData, player, chosen);
                }
            }
            gameData.resolvedMayAccepted = true;
            resumeEffectResolution(gameData);
            return false;
        }

        List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTapCostChoice(
                playerId, sourcePermanentId, tapCost, required));
        playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                handler.getPromptMessage(required));
        gameBroadcastService.broadcastGameState(gameData);
        return true;
    }

    public void completeTapCostChoice(GameData gameData, Player player,
                                      PermanentChoiceContext.MayAbilityTapCostChoice context,
                                      UUID chosenPermanentId) {
        UUID playerId = player.getId();
        PermanentChoiceCostHandler handler = new MultiplePermanentTapCostHandler(
                context.costEffect(), gameQueryService, gameBroadcastService, triggerCollectionService,
                context.sourcePermanentId());

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Invalid target permanent");
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || !battlefield.contains(chosen)) {
            throw new IllegalStateException("Must choose a permanent you control");
        }

        handler.validateAndPay(gameData, player, chosen);

        int remaining = context.remaining() - handler.lastPaymentWeight();
        if (remaining > 0) {
            if (!handler.canPayRemaining(gameData, playerId, remaining)) {
                throw new IllegalStateException("Not enough permanents remaining");
            }
            if (handler.shouldAutoPayAll(gameData, playerId, remaining)) {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                for (UUID id : validIds) {
                    Permanent autoPay = gameQueryService.findPermanentById(gameData, id);
                    if (autoPay != null) {
                        handler.validateAndPay(gameData, player, autoPay);
                    }
                }
            } else {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTapCostChoice(
                        playerId, context.sourcePermanentId(), context.costEffect(), remaining));
                playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                        handler.getPromptMessage(remaining));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        }

        gameData.resolvedMayAccepted = true;
        resumeEffectResolution(gameData);
    }

    private void resumeEffectResolution(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(
                    gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
