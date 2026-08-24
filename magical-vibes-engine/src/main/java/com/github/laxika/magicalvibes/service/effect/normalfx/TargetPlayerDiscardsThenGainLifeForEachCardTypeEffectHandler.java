package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingGainLifeOnDiscardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a target player's chosen discards and defers the type-counted life gain until the
 * discard interaction is complete.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsThenGainLifeForEachCardTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(targetPlayerId) + " has no cards to discard."));
            return;
        }

        gameData.pendingGainLifeOnDiscardType = new PendingGainLifeOnDiscardType(
                entry.getCard(), entry.getEntryType(), entry.getControllerId(), e.cardType(),
                e.lifePerCard(), 0);
        gameData.discardCausedByOpponent = !entry.getControllerId().equals(targetPlayerId);
        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, e.amount());
    }
}
