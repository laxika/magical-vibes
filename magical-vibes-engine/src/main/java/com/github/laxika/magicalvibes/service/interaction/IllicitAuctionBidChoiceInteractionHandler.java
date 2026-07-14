package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.XValueChoiceMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Prompts the current Illicit Auction bidder for a life bid, reusing the numeric X-value-choice wire
 * message. A value greater than {@code highBid} tops the bid; anything {@code <= highBid} passes. The
 * chosen value is stored on {@link GameData#chosenXValue} and effect resolution resumes, re-running
 * {@code IllicitAuctionEffectHandler} (kept alive via {@link GameData#rerunCurrentEffectAfterInteraction})
 * to advance the auction to the next bidder or finish it. Mirrors {@link XValueChoiceInteractionHandler}'s
 * resume sequence.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IllicitAuctionBidChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.IllicitAuctionBidChoice> {

    private final SessionManager sessionManager;
    private final GameBroadcastService gameBroadcastService;
    private final StateBasedActionService stateBasedActionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;

    @Override
    public Class<PendingInteraction.IllicitAuctionBidChoice> handledType() {
        return PendingInteraction.IllicitAuctionBidChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.IllicitAuctionBidChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.IllicitAuctionBidChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId,
                new XValueChoiceMessage(interaction.prompt(), interaction.maxBid(), interaction.cardName()));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to bid for {} (high bid {})",
                gameData.id, playerName, interaction.cardName(), interaction.highBid());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.IllicitAuctionBidChoice interaction,
                             InteractionAnswer answer) {
        int chosenValue = ((InteractionAnswer.NumberChosen) answer).value();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to bid");
        }
        if (chosenValue < 0 || chosenValue > interaction.maxBid()) {
            throw new IllegalArgumentException("Bid must be between 0 and " + interaction.maxBid());
        }

        gameData.chosenXValue = chosenValue;
        gameData.interaction.clearAwaitingInput();

        // Resume effect resolution (IllicitAuctionEffectHandler re-runs and reads chosenXValue).
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        if (!gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }
}
