package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfCreatureCardsExiledWithSourceEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCreatureCardsExiledWithSourceEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!context.targetOnSameBattlefield() || !hasCounteredCreature(context)) {
            return;
        }
        addAbilities(context, accumulator);
    }

    void addAbilities(StaticEffectContext context, StaticBonusAccumulator accumulator) {
        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.source().getId());
        for (Card card : exiledCards) {
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }
            for (ActivatedAbility ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(ability);
            }
            List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                accumulator.addActivatedAbility(new ActivatedAbility(
                        true, null, onTapEffects, "{T}: Add mana."));
            }
        }
    }

    boolean hasCounteredCreature(StaticEffectContext context) {
        Permanent target = context.target();
        return target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                && support.isEffectivelyCreature(context.gameData(), target,
                support.hasAnimateArtifactEffect(context.gameData()));
    }
}
