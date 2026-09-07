package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCardFromControllerHandToDiscardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reveals the controller's hand and lets the target player choose one card for the controller to
 * discard. The existing {@link PendingInteraction.RevealedHandChoice} handler completes the
 * discard and resumes the rest of the ability.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesCardFromControllerHandToDiscardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCardFromControllerHandToDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID choosingPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String choosingPlayerName = gameData.playerIdToName.get(choosingPlayerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " reveals their hand. It is empty."));
            log.info("Game {} - {}'s hand is empty for opponent-chosen discard", gameData.id, controllerName);
            return;
        }

        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        gameLogService.append(gameData, GameLog.text(controllerName + " reveals their hand: " + cardNames + "."));

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        // The ability itself causes its controller's discard, even though the target chooses the card.
        gameData.discardCausedByOpponent = false;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                choosingPlayerId, controllerId, validIndices, 1, true, false,
                new ArrayList<>(), entry.getSourcePermanentId(),
                choosingPlayerName + " chooses a card for " + controllerName + " to discard.",
                false, false, false));

        log.info("Game {} - {} choosing a card from {}'s hand to discard", gameData.id,
                choosingPlayerName, controllerName);
    }
}
