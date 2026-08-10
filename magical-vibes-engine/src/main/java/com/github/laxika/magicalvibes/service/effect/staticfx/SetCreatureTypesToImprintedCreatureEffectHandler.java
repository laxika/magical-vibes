package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetCreatureTypesToImprintedCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetCreatureTypesToImprintedCreatureEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetCreatureTypesToImprintedCreatureEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card imprinted = context.gameData().getImprintedCard(context.source().getCard());
        if (imprinted == null
                || context.gameData().findExiledCard(imprinted.getId()) == null
                || !imprinted.hasType(CardType.CREATURE)) {
            return;
        }

        SetCreatureTypesToImprintedCreatureEffect setTypes =
                (SetCreatureTypesToImprintedCreatureEffect) effect;
        imprinted.getSubtypes().stream()
                .filter(StaticEffectSupport::isCreatureSubtype)
                .forEach(accumulator::addGrantedSubtype);
        for (CardSubtype retainedSubtype : setTypes.retainedSubtypes()) {
            accumulator.addGrantedSubtype(retainedSubtype);
        }
        accumulator.setSubtypeOverriding(true);
    }
}
