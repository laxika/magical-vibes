package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingUntapOnDiscardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffect) effect;
        UUID targetPlayerId = entry.getTargetId();

        drawService.resolveDrawCard(gameData, targetPlayerId);

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null && targetHand != null && !targetHand.isEmpty()) {
            gameData.pendingUntapOnDiscardType =
                    new PendingUntapOnDiscardType(sourcePermanentId, e.untapIfType());
        }

        gameData.discardCausedByOpponent = !entry.getControllerId().equals(targetPlayerId);
        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, 1);
    }
}
