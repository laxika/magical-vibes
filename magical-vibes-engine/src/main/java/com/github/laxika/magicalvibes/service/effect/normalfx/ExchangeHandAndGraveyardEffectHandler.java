package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeHandAndGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the controller's hand/graveyard exchange. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeHandAndGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeHandAndGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> handCards = hand == null ? List.of() : new ArrayList<>(hand);
        List<Card> graveyardCards = graveyard == null
                ? List.of()
                : graveyard.stream().filter(card -> !card.isToken()).toList();

        if (hand != null) {
            hand.clear();
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            if (graveyard != null) {
                graveyardService.clearGraveyard(gameData, playerId);
            }
            if (hand != null) {
                hand.addAll(graveyardCards);
            }
            for (Card card : handCards) {
                graveyardService.addCardToGraveyard(gameData, playerId, card, Zone.HAND);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exchanges their hand and graveyard."));
        log.info("Game {} - {} exchanges their hand ({} cards) and graveyard ({} cards)",
                gameData.id, playerName, handCards.size(), graveyardCards.size());
    }
}
