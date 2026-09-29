package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsExiledWithBrainCountersEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Supplies Rex with the activated abilities of brain-counter cards in exile. */
@Component
public class GainActivatedAbilitiesOfCardsExiledWithBrainCountersSelfEffectHandler
        implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCardsExiledWithBrainCountersEffect.class;
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
                if (context.gameData().exiledCardsWithBrainCounters.contains(entry.card().getId())) {
                    cards.add(entry.card());
                }
            }
        }

        for (Card card : cards) {
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
