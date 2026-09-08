package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "179")
public class ElderGargaroth extends Card {

    public ElderGargaroth() {
        ChooseOneEffect attackOrBlockEffect = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 3/3 green Beast creature token.",
                        new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN,
                                List.of(CardSubtype.BEAST), Set.of(), Set.of())
                ),
                new ChooseOneEffect.ChooseOneOption("You gain 3 life.", new GainLifeEffect(3)),
                new ChooseOneEffect.ChooseOneOption("Draw a card.", new DrawCardEffect(1))
        ));

        addEffect(EffectSlot.ON_ATTACK, attackOrBlockEffect);
        addEffect(EffectSlot.ON_BLOCK, attackOrBlockEffect);
    }
}
