package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerDiscardsThenDrawsThatManyEffect} (Forget): the target player discards
 * {@code amount} cards, then draws as many as they discarded this way. The discard is mandatory, so
 * the number actually discarded is {@code min(amount, handSize)} — snapshotted before the discard so
 * the rummage follow-up draws that many to the same player once the (interactive) discard completes.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDiscardsThenDrawsThatManyEffect) effect;

        UUID playerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(playerId);
        int drawCount = Math.min(e.amount(), hand == null ? 0 : hand.size());
        if (drawCount <= 0) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, e.amount(),
                DiscardFollowUp.rummage(drawCount));
    }
}
