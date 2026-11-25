package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HOB", collectorNumber = "35")
public class ConfusticateAndBebother extends Card {

    public ConfusticateAndBebother() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {4}",
                        UnlessPaysEffect.counterUnlessPays(4)),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw two cards, then discard a card",
                        List.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, Subjects.you())))
        )));
    }
}
