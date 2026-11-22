package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachOpponentControllingReturnedPermanentEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.stereotype.Component;

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

        DrawnThisWay snapshot = DrawnThisWay.before(gameData, entry.getControllerId());
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), amount);
        snapshot.cards(gameData).forEach(card -> entry.recordCardDrawnThisResolution(card.getId()));
    }
}
