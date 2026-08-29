package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        var permission = (AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect) effect;
        List<Card> matchingCards = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> permission.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))
                .toList();
        for (Card card : matchingCards) {
            gameData.exilePlayPermissions.put(card.getId(), entry.getControllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            gameData.exilePlayAnyManaType.add(card.getId());
        }
    }
}
