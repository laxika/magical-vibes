package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "72")
public class ThassasIntervention extends Card {

    public ThassasIntervention() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top X cards of your library. Put up to two of them into your hand and the rest on the bottom of your library in a random order",
                        LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(new XValue(), 2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays twice {X}",
                        new CounterUnlessPaysEffect(new Scaled(new XValue(), 2)))
        )));
    }
}
