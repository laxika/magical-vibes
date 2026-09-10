package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsAnyNumberThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a target player's chosen discard followed by an equal number of draws. */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsAnyNumberThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsAnyNumberThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(playerId);

        if (gameData.chosenXValue != null) {
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            entry.setEventValue(chosenCount);

            if (chosenCount == 0) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + " chooses to discard 0 cards for " + cardName + "."));
                return;
            }

            gameData.discardCausedByOpponent = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, chosenCount,
                    DiscardFollowUp.rummage(chosenCount));
            return;
        }

        entry.setEventValue(0);
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " has no cards to discard for " + cardName + "."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(playerId, hand.size(),
                        "Discard any number of cards for " + cardName + ".", cardName));
    }
}
