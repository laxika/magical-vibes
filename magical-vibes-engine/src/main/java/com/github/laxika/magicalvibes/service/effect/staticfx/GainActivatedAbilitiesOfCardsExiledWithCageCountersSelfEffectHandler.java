package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsExiledWithCageCountersEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Supplies Mairsil with once-per-turn activated abilities from its owner's caged cards. */
@Component
public class GainActivatedAbilitiesOfCardsExiledWithCageCountersSelfEffectHandler
        implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCardsExiledWithCageCountersEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        List<Card> cards = new ArrayList<>();
        synchronized (context.gameData().exiledCards) {
            for (ExiledCardEntry entry : context.gameData().exiledCards) {
                if (context.sourceControllerId().equals(entry.ownerId())
                        && context.gameData().exiledCardsWithCageCounters.contains(entry.card().getId())) {
                    cards.add(entry.card());
                }
            }
        }

        for (Card card : cards) {
            for (ActivatedAbility ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(ability.withMaxActivationsPerTurn(1));
            }
            List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                accumulator.addActivatedAbility(new ActivatedAbility(
                        true, null, onTapEffects, "{T}: Add mana.").withMaxActivationsPerTurn(1));
            }
        }
    }
}
