package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "35")
public class ConfusticateAndBebother extends Card {

    public ConfusticateAndBebother() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {4}",
                        new CounterUnlessPaysEffect(4)),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw two cards, then discard a card",
                        List.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)))
        )));
    }
}
