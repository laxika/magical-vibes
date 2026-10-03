package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachOpponentControllingReturnedPermanentEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class DrawCardForEachOpponentControllingReturnedPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;

    public DrawCardForEachOpponentControllingReturnedPermanentEffectHandler(
            PlayerInteractionSupport playerInteractionSupport,
            AmountEvaluationService amountEvaluationService) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.amountEvaluationService = amountEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForEachOpponentControllingReturnedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var drawEffect = (DrawCardForEachOpponentControllingReturnedPermanentEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, drawEffect.drawnCardAmount(),
                AmountContext.forStackEntry(entry, null));

        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        Set<UUID> cardsInHandBeforeDraw = new HashSet<>();
        hand.forEach(card -> cardsInHandBeforeDraw.add(card.getId()));
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), amount);
        gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> !cardsInHandBeforeDraw.contains(card.getId()))
                .forEach(card -> entry.recordCardDrawnThisResolution(card.getId()));
    }
}
