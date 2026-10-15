package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "246")
public class SteelExemplar extends Card {

    public SteelExemplar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new NotCondition(twoOrMoreColorsSpent()),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
    }

    private static Condition twoOrMoreColorsSpent() {
        return new AnyOf(List.of(
                colorsSpent(ManaColor.WHITE, ManaColor.BLUE),
                colorsSpent(ManaColor.WHITE, ManaColor.BLACK),
                colorsSpent(ManaColor.WHITE, ManaColor.RED),
                colorsSpent(ManaColor.WHITE, ManaColor.GREEN),
                colorsSpent(ManaColor.BLUE, ManaColor.BLACK),
                colorsSpent(ManaColor.BLUE, ManaColor.RED),
                colorsSpent(ManaColor.BLUE, ManaColor.GREEN),
                colorsSpent(ManaColor.BLACK, ManaColor.RED),
                colorsSpent(ManaColor.BLACK, ManaColor.GREEN),
                colorsSpent(ManaColor.RED, ManaColor.GREEN)));
    }

    private static Condition colorsSpent(ManaColor first, ManaColor second) {
        return new AllConditions(List.of(new ColorSpentToCast(first), new ColorSpentToCast(second)));
    }
}
