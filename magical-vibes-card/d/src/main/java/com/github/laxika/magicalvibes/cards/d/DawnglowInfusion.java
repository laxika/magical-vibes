package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SHM", collectorNumber = "225")
public class DawnglowInfusion extends Card {

    public DawnglowInfusion() {
        // Gain X life if {G} was spent to cast this spell...
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN),
                new GainLifeEffect(new XValue())));

        // ...and X life if {W} was spent to cast this spell (both if {G}{W} was spent).
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                new GainLifeEffect(new XValue())));
    }
}
