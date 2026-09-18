package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfNextPlayerNonlandPermanentsEffect.Direction;
import com.github.laxika.magicalvibes.model.effect.OrderOfSuccessionEffect;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "52")
public class OrderOfSuccession extends Card {

    public OrderOfSuccession() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Left",
                        new OrderOfSuccessionEffect(Direction.LEFT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Right",
                        new OrderOfSuccessionEffect(Direction.RIGHT))
        )));
    }
}
