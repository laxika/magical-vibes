package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenControllerDrawsShortfallEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a target's mandatory discard and draws the controller's shortfall. */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsThenControllerDrawsShortfallEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsThenControllerDrawsShortfallEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDiscardsThenControllerDrawsShortfallEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (targetPlayerId == null) {
            return;
        }

        int discardAmount = Math.max(0, e.amount());
        if (discardAmount == 0) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int actualDiscardAmount = Math.min(discardAmount, hand == null ? 0 : hand.size());
        int drawAmount = discardAmount - actualDiscardAmount;
        if (hand == null || hand.isEmpty()) {
            String targetName = gameData.playerIdToName.get(targetPlayerId);
            String controllerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(targetName + " has no cards to discard. "
                    + controllerName + " draws " + drawAmount + " cards."));
            playerInteractionSupport.applyDrawCards(gameData, controllerId, drawAmount);
            return;
        }

        gameData.discardCausedByOpponent = !controllerId.equals(targetPlayerId);
        DiscardFollowUp followUp = drawAmount == 0
                ? DiscardFollowUp.NONE
                : DiscardFollowUp.rummageToPlayer(drawAmount, controllerId);
        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, discardAmount, followUp);
    }
}
