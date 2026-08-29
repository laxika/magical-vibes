package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfExiledCardsSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfExiledCardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GainActivatedAbilitiesOfExiledCardsEffect gainEffect =
                (GainActivatedAbilitiesOfExiledCardsEffect) effect;
        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.source().getId());
        if (exiledCards.isEmpty()) return;
        for (Card card : exiledCards) {
            for (var ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(gainEffect.oncePerTurn()
                        ? ability.withMaxActivationsPerTurn(1)
                        : ability);
            }
            List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                ActivatedAbility manaAbility = new ActivatedAbility(
                        true, null, onTapEffects, "{T}: Add mana.");
                accumulator.addActivatedAbility(gainEffect.oncePerTurn()
                        ? manaAbility.withMaxActivationsPerTurn(1)
                        : manaAbility);
            }
        }
    }
}
