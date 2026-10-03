package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawDiscardThenEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawDiscardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawDiscardThenEffect drawDiscardThen = (DrawDiscardThenEffect) effect;
        UUID controllerId = entry.getControllerId();

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData,
                drawDiscardThen.amount(), AmountContext.forStackEntry(entry, source)));

        playerInteractionSupport.applyDrawCards(gameData, controllerId, amount);
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        int discardAmount = Math.min(amount, hand.size());
        if (discardAmount == 0) {
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, discardAmount,
                DiscardFollowUp.thenEffectWithEventValueAndDiscardedCards(
                        entry.getCard(), drawDiscardThen.thenEffect(), discardAmount,
                        entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot(), controllerId));
    }
}
