package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BLB", collectorNumber = "45")
public class DazzlingDenial extends Card {

    public DazzlingDenial() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                new CounterUnlessPaysEffect(2),
                new CounterUnlessPaysEffect(4)
        ));
    }
}
