package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSR", collectorNumber = "312")
@CardRegistration(set = "MAR", collectorNumber = "12")
public class MysticConfluence extends Card {

    public MysticConfluence() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {3}",
                        new CounterUnlessPaysEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature to its owner's hand",
                        ReturnToHandEffect.target(), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw a card",
                        new DrawCardEffect())
        ), 3));
    }
}
