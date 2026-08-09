package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenSubtypeSpellsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForChosenSubtypeSpellsEffectHandler implements CostModificationHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForChosenSubtypeSpellsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId()) || source.sourcePermanent() == null) {
            return 0;
        }
        var chosenSubtype = source.sourcePermanent().getChosenSubtype();
        var spell = context.spell();
        if (chosenSubtype == null || !spell.hasType(CardType.CREATURE)) {
            return 0;
        }
        boolean hasChosenSubtype = spell.getKeywords().contains(Keyword.CHANGELING)
                || gameQueryService.cardHasSubtype(spell, chosenSubtype, context.gameData(), spell.getOwnerId());
        return hasChosenSubtype
                ? -((ReduceCastCostForChosenSubtypeSpellsEffect) effect).amount()
                : 0;
    }
}
