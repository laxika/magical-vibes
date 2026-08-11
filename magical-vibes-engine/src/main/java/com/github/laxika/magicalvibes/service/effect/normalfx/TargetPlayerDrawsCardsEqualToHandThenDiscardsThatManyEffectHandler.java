package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Laquatus's Creativity's draw-then-discard effect. */
@Component
@RequiredArgsConstructor
public class TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffectHandler
        implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(playerId);
        int count = hand == null ? 0 : hand.size();
        if (count == 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, playerId);
            if (gameData.status == GameStatus.FINISHED) {
                return;
            }
        }

        gameData.discardCausedByOpponent = !entry.getControllerId().equals(playerId);
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, count);
    }
}
