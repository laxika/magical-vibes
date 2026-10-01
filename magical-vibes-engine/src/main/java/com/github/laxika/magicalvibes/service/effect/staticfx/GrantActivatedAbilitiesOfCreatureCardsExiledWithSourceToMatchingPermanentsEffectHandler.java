package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect,
                      StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect) effect;
        if (!context.targetOnSameBattlefield()
                || !support.matchesStaticFilter(context, context.target(), grant.targetFilter())) {
            return;
        }

        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.sourceId());
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
}
