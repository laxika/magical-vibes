package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect,
                      StaticBonusAccumulator accumulator) {
        if (!context.targetOnSameBattlefield()
                || !support.isEffectivelyCreature(context.gameData(), context.target(),
                support.hasAnimateArtifactEffect(context.gameData()))) {
            return;
        }

        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.sourceId());
        for (Card card : exiledCards) {
            if (!card.hasType(CardType.LAND)) {
                continue;
            }
            for (ActivatedAbility ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(ability);
            }
            List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                accumulator.addActivatedAbility(new ActivatedAbility(
                        true, null, onTapEffects, "{T}: Add mana."
                ));
            }
        }
    }
}
