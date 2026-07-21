package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "49")
public class SupremeWill extends Card {

    public SupremeWill() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {3}",
                        new CounterUnlessPaysEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top four cards of your library. Put one of them into your "
                                + "hand and the rest on the bottom of your library in any order",
                        LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(4)))
        )));
    }
}
