package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles scry interactions: prompts the deciding player with the looked-at cards and,
 * on answer, validates the top/bottom split and puts the cards back on the library.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScryInteractionHandler implements InteractionHandler<PendingInteraction.Scry> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;

    @Override
    public Class<PendingInteraction.Scry> handledType() {
        return PendingInteraction.Scry.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ScryOrder.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.Scry interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.cards().stream().map(cardViewFactory::create).toList();
        int n = interaction.cards().size();
        String prompt;
        if (interaction.toGraveyard()) {
            prompt = n == 1
                    ? "Surveil 1: Keep on top of your library or put into your graveyard."
                    : "Surveil " + n + ": Put cards on top of your library or into your graveyard.";
        } else {
            prompt = n == 1
                    ? "Scry 1: Keep on top or put on the bottom of your library."
                    : "Scry " + n + ": Put cards on the top or bottom of your library.";
        }
        sessionManager.sendToPlayer(recipientId,
                InteractionPromptMessage.scryOrder(cardViews, prompt, interaction.toGraveyard()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.Scry interaction,
                             InteractionAnswer answer) {
        List<Integer> topCardOrder = ((InteractionAnswer.ScryOrder) answer).topCardOrder();
        List<Integer> bottomCardOrder = ((InteractionAnswer.ScryOrder) answer).bottomCardOrder();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to scry");
        }

        List<Card> scryCards = interaction.cards();
        int count = scryCards.size();

        if (topCardOrder.size() + bottomCardOrder.size() != count) {
            throw new IllegalStateException("Must assign all " + count + " cards");
        }

        // Validate indices are a valid permutation of 0..count-1
        Set<Integer> seen = new HashSet<>();
        for (int idx : topCardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }
        for (int idx : bottomCardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }

        List<Card> deck = gameData.playerDecks.get(player.getId());

        // Put top cards on top of library in order (first in list = top of library)
        for (int i = topCardOrder.size() - 1; i >= 0; i--) {
            deck.add(0, scryCards.get(topCardOrder.get(i)));
        }

        if (interaction.toGraveyard()) {
            // Surveil: the reject pile goes into the graveyard in the chosen order.
            List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
            for (int idx : bottomCardOrder) {
                graveyard.add(scryCards.get(idx));
            }
        } else {
            // Scry: the reject pile goes to the bottom of the library in order.
            for (int idx : bottomCardOrder) {
                deck.add(scryCards.get(idx));
            }
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();

        String logMsg;
        if (interaction.toGraveyard()) {
            if (bottomCardOrder.isEmpty()) {
                logMsg = player.getUsername() + " keeps " + count + " card(s) on top of their library (surveil).";
            } else if (topCardOrder.isEmpty()) {
                logMsg = player.getUsername() + " puts " + count + " card(s) into their graveyard (surveil).";
            } else {
                logMsg = player.getUsername() + " keeps " + topCardOrder.size() + " card(s) on top and puts "
                        + bottomCardOrder.size() + " into their graveyard (surveil).";
            }
        } else if (bottomCardOrder.isEmpty()) {
            logMsg = player.getUsername() + " puts " + count + " card(s) on top of their library.";
        } else if (topCardOrder.isEmpty()) {
            logMsg = player.getUsername() + " puts " + count + " card(s) on the bottom of their library.";
        } else {
            logMsg = player.getUsername() + " puts " + topCardOrder.size() + " card(s) on top and "
                    + bottomCardOrder.size() + " on the bottom of their library.";
        }
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} {} completed: {} top, {} reject", gameData.id, player.getUsername(),
                interaction.toGraveyard() ? "surveil" : "scry", topCardOrder.size(), bottomCardOrder.size());

        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability
        // (e.g. Foresee: "Scry 4, then draw two cards.")
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
}
