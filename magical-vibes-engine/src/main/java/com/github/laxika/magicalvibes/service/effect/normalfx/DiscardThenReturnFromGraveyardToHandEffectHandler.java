package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardThenReturnFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DiscardThenReturnFromGraveyardToHandEffect} (Recall): the controller discards
 * {@code amount} cards, then returns a card from their graveyard to their hand for each card
 * discarded this way. The discard is mandatory, so the number actually discarded — and therefore
 * the number returned — is {@code min(amount, hand size)}, snapshotted before the discard so the
 * graveyard-return follow-up returns that many once the (interactive) discard completes.
 */
@Component
@RequiredArgsConstructor
public class DiscardThenReturnFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardThenReturnFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardThenReturnFromGraveyardToHandEffect) effect;

        UUID playerId = entry.getControllerId();
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, null));

        List<Card> hand = gameData.playerHands.get(playerId);
        int returnCount = Math.min(amount, hand == null ? 0 : hand.size());
        if (returnCount <= 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " discards no cards."));
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, amount,
                DiscardFollowUp.graveyardReturn(returnCount));
    }
}
