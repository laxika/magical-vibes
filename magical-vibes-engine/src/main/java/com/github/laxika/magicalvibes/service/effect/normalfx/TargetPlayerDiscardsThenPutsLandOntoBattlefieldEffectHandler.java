package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Mind Roots. The target player chooses the discarded cards; once that choice completes,
 * the spell controller may choose one land among those exact discarded cards to enter tapped.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int discardCount = Math.min(e.discardAmount(), hand == null ? 0 : hand.size());
        if (discardCount <= 0) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(targetPlayerId) + " has no cards to discard."));
            return;
        }

        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, e.discardAmount(),
                DiscardFollowUp.chooseDiscardedLandForBattlefield(entry.getControllerId()));
    }
}
