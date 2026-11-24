package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KeywordCost;
import com.github.laxika.magicalvibes.model.effect.ModifyKeywordCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Battlefield handler for {@link ModifyKeywordCostEffect}: each {@link KeywordCost} changes only its own cost channel
 * (buyback, the dash alternative cost, a flashback cast, a morph cost, a Room unlock). Cycling and ninjutsu are
 * activation costs, read by {@code CastingCostService.getCyclingAbilityCostReduction} and
 * {@code getNinjutsuAbilityCostReduction}; ordinary spell costs are never changed.
 */
@Component
public class ModifyKeywordCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ModifyKeywordCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return context.flashbackCost() ? change(effect, KeywordCost.FLASHBACK, source, context.castingPlayerId()) : 0;
    }

    @Override
    public int modifyBuybackCost(CostModificationContext context, CardEffect effect,
                                 CostModificationSource source) {
        return change(effect, KeywordCost.BUYBACK, source, context.castingPlayerId());
    }

    @Override
    public int modifyAlternateCost(CostModificationContext context, CardEffect effect,
                                   CostModificationSource source) {
        return context.spell().getKeywords().contains(Keyword.DASH)
                ? change(effect, KeywordCost.DASH, source, context.castingPlayerId()) : 0;
    }

    @Override
    public int modifyMorphCost(CostModificationContext context, CardEffect effect,
                               CostModificationSource source) {
        if (source.sourcePermanent() != null && source.sourcePermanent().isFaceDown()) {
            return 0;
        }
        return change(effect, KeywordCost.MORPH, source, context.castingPlayerId());
    }

    @Override
    public int modifyRoomUnlockCost(GameData gameData, UUID playerId, Card room, CardEffect effect,
                                    CostModificationSource source) {
        return change(effect, KeywordCost.UNLOCK, source, playerId);
    }

    private static int change(CardEffect effect, KeywordCost channel, CostModificationSource source, UUID payerId) {
        var modify = (ModifyKeywordCostEffect) effect;
        return modify.cost() == channel && modify.scope().names(source.controllerId(), payerId) ? modify.amount() : 0;
    }
}
