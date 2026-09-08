package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingConnive;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawBeforeConniveReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawDiscardAndConniveEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

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
        UUID sourcePermanentId = e.useEnteringPermanentReference()
                ? entry.getTargetId() != null ? entry.getTargetId() : entry.getTriggeringPermanentId()
                : e.targetPermanent()
                ? targetIds == null ? entry.getTargetId() : targetIds.stream().findFirst().orElse(entry.getTargetId())
                : entry.getSourcePermanentId();
        if (e.targetPermanent() && sourcePermanentId == null) {
            return;
        }

        int replacementDraws = gameQueryService.countPlayerControlledStaticEffects(
                gameData, controllerId, DrawBeforeConniveReplacementEffect.class);
        for (int i = 0; i < replacementDraws; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source)));

        playerInteractionSupport.applyDrawCards(gameData, controllerId, amount);
        gameData.pendingConnive = null;
        List<Card> hand = gameData.playerHands.get(controllerId);
        int discardAmount = Math.min(amount, hand == null ? 0 : hand.size());
        if (sourcePermanentId != null && discardAmount > 0) {
            gameData.pendingConnive = new PendingConnive(sourcePermanentId, discardAmount);
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, discardAmount);
    }
}
