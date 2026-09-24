package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ModifyCastCostForCardsDrawnThisTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ModifyCastCostForCardsDrawnThisTurnEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ModifyCastCostForCardsDrawnThisTurnEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect,
                          CostModificationSource source) {
        UUID sourceControllerId = source.controllerId();
        if (sourceControllerId == null || context.spell() == null) {
            return 0;
        }

        var modifier = (ModifyCastCostForCardsDrawnThisTurnEffect) effect;
        boolean matchingCard = modifier.opponentsCards()
                ? wasDrawnByOpponent(context.gameData(), sourceControllerId, context.spell().getId())
                : wasDrawnBy(context.gameData(), sourceControllerId, context.spell().getId());
        if (!matchingCard) {
            return 0;
        }
        return modifier.opponentsCards() ? modifier.amount() : -modifier.amount();
    }

    private boolean wasDrawnBy(GameData gameData, UUID playerId, UUID cardId) {
        return gameData.cardsDrawnThisTurnIds.getOrDefault(playerId, List.of()).contains(cardId);
    }

    private boolean wasDrawnByOpponent(GameData gameData, UUID controllerId, UUID cardId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .anyMatch(playerId -> wasDrawnBy(gameData, playerId, cardId));
    }
}
