package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSharedCardTypeWithImprintEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.cast.CostModificationSupport;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler implements CostModificationHandlerBean {

    private final CostModificationSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostForSharedCardTypeWithImprintEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostForSharedCardTypeWithImprintEffect) effect;
        if (!source.controlledBy(context.castingPlayerId())) {
            return 0;
        }
        GameData gameData = context.gameData();
        // Every card this permanent exiled and that is still exiled counts (several imprint
        // triggers can each exile one); the single recorded imprint covers sources that did not
        // track their exile with the permanent.
        List<Card> imprintedCards = new ArrayList<>(
                gameData.getCardsExiledByPermanent(source.sourcePermanent().getId()));
        Card imprinted = gameData.getImprintedCard(source.sourcePermanent().getCard());
        if (imprinted != null && gameData.findExiledCard(imprinted.getId()) != null
                && imprintedCards.stream().noneMatch(card -> card.getId().equals(imprinted.getId()))) {
            imprintedCards.add(imprinted);
        }
        if (imprintedCards.stream().noneMatch(card -> support.sharesCardType(context.spell(), card))) {
            return 0;
        }
        int amount = amountEvaluationService.evaluate(context.gameData(), reduce.amount(),
                AmountContext.forCasting(context.castingPlayerId()));
        return -amount;
    }
}
