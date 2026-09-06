package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "76")
public class Complicate extends Card {

    public Complicate() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new MayEffect(
                                new CounterUnlessPaysEffect(1),
                                "Counter target spell unless its controller pays {1}?"),
                        new DrawCardEffect(1)),
                "Cycling {2}{U} ({2}{U}, Discard this card: Draw a card.)",
                null,
                null,
                null,
                null,
                List.of(),
                0,
                1));
    }
}
