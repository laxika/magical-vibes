package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "RAV", collectorNumber = "56")
public class InduceParanoia extends Card {

    public InduceParanoia() {
        // The countering step records the spell's mana value and controller on the resolving entry,
        // so the mill that follows reads the countered spell's last known values.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLACK),
                new MillEffect(new TargetSpellManaValue(), MillRecipient.TARGET_SPELL_CONTROLLER)));
    }
}
