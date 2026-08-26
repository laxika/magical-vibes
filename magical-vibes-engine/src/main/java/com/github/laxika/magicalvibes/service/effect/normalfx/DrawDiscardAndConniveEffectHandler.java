package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingConnive;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawDiscardAndConniveEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawDiscardAndConniveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawDiscardAndConniveEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> targetIds = e.targetPermanent()
                ? entry.targetsForEffect(effect)
                : null;
        UUID sourcePermanentId = e.targetPermanent()
                ? targetIds == null ? null : targetIds.stream().findFirst().orElse(null)
                : entry.getSourcePermanentId();
        if (e.targetPermanent() && sourcePermanentId == null) {
            return;
        }

        drawService.resolveDrawCard(gameData, controllerId);
        gameData.pendingConnive = null;
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (sourcePermanentId != null && hand != null && !hand.isEmpty()) {
            gameData.pendingConnive = new PendingConnive(sourcePermanentId);
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1);
    }
}
