package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardThenDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a target player's random discard followed by a conditional draw. */
@Component
@RequiredArgsConstructor
public class TargetPlayerRandomDiscardThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerRandomDiscardThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        int discardedBefore = gameData.cardsDiscardedThisTurn.getOrDefault(targetPlayerId, 0);
        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveRandomDiscardCards(
                gameData, targetPlayerId, entry.getCard().getName(), 1);

        int discardedAfter = gameData.cardsDiscardedThisTurn.getOrDefault(targetPlayerId, 0);
        if (discardedAfter > discardedBefore) {
            drawService.resolveDrawCard(gameData, targetPlayerId);
        }
    }
}
