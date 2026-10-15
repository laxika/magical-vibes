package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "38")
public class SpiritOfResistance extends Card {

    public SpiritOfResistance() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        controlsPermanentOf(CardColor.WHITE),
                        controlsPermanentOf(CardColor.BLUE),
                        controlsPermanentOf(CardColor.BLACK),
                        controlsPermanentOf(CardColor.RED),
                        controlsPermanentOf(CardColor.GREEN))),
                new PreventAllDamageToControllerEffect()));
    }

    private static ControlsPermanent controlsPermanentOf(CardColor color) {
        return new ControlsPermanent(new PermanentColorInPredicate(Set.of(color)));
    }
}
